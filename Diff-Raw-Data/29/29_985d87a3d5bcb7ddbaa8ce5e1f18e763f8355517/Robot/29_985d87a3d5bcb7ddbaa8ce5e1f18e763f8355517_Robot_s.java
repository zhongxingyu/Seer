 package com.github.thehilikus.jrobocom.robot;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.EventListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.thehilikus.events.event_manager.api.EventDispatcher;
 import com.github.thehilikus.events.event_manager.api.EventPublisher;
 import com.github.thehilikus.jrobocom.Direction;
 import com.github.thehilikus.jrobocom.GameSettings;
 import com.github.thehilikus.jrobocom.Player;
 import com.github.thehilikus.jrobocom.World;
 import com.github.thehilikus.jrobocom.events.RobotChangedEvent;
 import com.github.thehilikus.jrobocom.exceptions.BankInterruptedException;
 import com.github.thehilikus.jrobocom.player.Bank;
 import com.github.thehilikus.jrobocom.player.InstructionSet;
 import com.github.thehilikus.jrobocom.player.ScanResult;
 import com.github.thehilikus.jrobocom.robot.api.RobotAction;
 import com.github.thehilikus.jrobocom.robot.api.RobotStatusLocal;
 import com.github.thehilikus.jrobocom.timing.Delayer;
 
 /**
  * Keeps the state of each robot in the board
  * 
  * @author hilikus
  * 
  */
 public class Robot implements RobotAction, Runnable, EventPublisher {
 
     private static int lastSerial = 0;
 
     private final TurnManager turnsControl;
 
     private final World world;
 
     private Logger log = LoggerFactory.getLogger(Robot.class);
 
     private RobotData data;
 
     private boolean alive;
 
     private boolean pendingBankChange;
 
     private final Bank[] banks;
 
     private int runningBank;
 
     private int serialNumber;
 
     private final String name;
 
     private final Player owner;
 
     private EventDispatcher eventDispatcher;
 
     private volatile boolean interrupted = false;
 
     /**
      * Events interface
      * 
      */
     public interface RobotListener extends EventListener {
 	/**
 	 * @param evt the change object
 	 */
 	public void update(RobotChangedEvent evt);
     }
 
     /**
      * Common constructor for all robots
      * 
      * @param theWorld the world the robots lives in
      * @param delayer the instance that blocks turns
      * @param banksCount number of banks
      */
     private Robot(World theWorld, Delayer delayer, int banksCount, String pName, Player pOwner) {
 	if (theWorld == null || delayer == null || pOwner == null) {
 	    throw new IllegalArgumentException("Arguments cannot be null");
 	}
 	serialNumber = Robot.getNextSerialNumber();
 
 	world = theWorld;
 	turnsControl = new TurnManager(delayer);
 	banks = new Bank[banksCount];
 	owner = pOwner;
 	name = pName;
     }
 
     /**
      * Creates first robot in the world. It is the responsibility of the caller to start the robot's
      * thread
      * 
      * @param theWorld the environment of the robot
      * @param delayer the ticker to control turns
      * @param allBanks the code to execute
      * @param name this robot's name
      * @param pOwner the player that created this robot
      */
     public Robot(World theWorld, Delayer delayer, Bank[] allBanks, String name, Player pOwner) {
 	this(theWorld, delayer, allBanks.length, name, pOwner);
 
 	Direction randomDir = Direction.fromInt(World.getRandGenerator().nextInt(Direction.COUNT));
 	data = new RobotData(this, InstructionSet.SUPER, false, 0, randomDir);
 
 	for (int pos = 0; pos < allBanks.length; pos++) {
 	    setBank(allBanks[pos], pos, false);
 	}
 
 	alive = data.getGeneration() < GameSettings.getInstance().MAX_GENERATION;
     }
 
     /**
      * Creates a child robot. It is the responsibility of the caller to start the robot's thread
      * 
      * @param pSet maximum instruction set supported
      * @param banksCount number of banks
      * @param pMobile true if robot is mobile
      * @param parent creator of this robot
      * @param name a name of this single robot
      */
     private Robot(InstructionSet pSet, int banksCount, boolean pMobile, Robot parent, String name) {
 	this(parent.world, parent.getTurnsControl().delayer, banksCount, name, parent.owner);
 
 	if (banksCount > GameSettings.getInstance().MAX_BANKS) {
 	    throw new IllegalArgumentException("Too many banks");
 	}
 	if (parent.data.getInstructionSet().isLessThan(InstructionSet.SUPER)) {
 	    throw new IllegalArgumentException("Parent could not have created child");
 	}
 
 	data = new RobotData(this, pSet, pMobile, parent.data.getGeneration() + 1, parent.data.getFacing());
     }
 
     @Override
     public int reverseTransfer(int remoteSource, int localDestination) {
 	if (data.getInstructionSet().isLessThan(InstructionSet.ADVANCED)) {
 	    die("Invalid action in Instruction Set");
 	} else {
 	    Robot neighbour = world.getNeighbour(this);
 	    if (neighbour != null) {
 		Bank remoteBank = neighbour.getBankCopy(remoteSource, true);
 		if (remoteBank != null) {
 		    boolean success = setBank(remoteBank, localDestination, false);
 		    if (success) {
 			return remoteBank.getCost();
 		    }
 		}
 	    }
 	}
 	return 0;
     }
 
     @Override
     public int transfer(int localSource, int remoteDestination) {
 	if (data.getInstructionSet().isLessThan(InstructionSet.ADVANCED)) {
 	    die("Invalid action in Instruction Set");
 	} else if (localSource < 0 || localSource >= banks.length) {
 	    die("Invalid transfer position attempted");
 	} else {
 	    Robot neighbour = world.getNeighbour(Robot.this);
 	    if (neighbour != null) {
 		Bank localBankCopy;
 		try {
 		    localBankCopy = getBankCopy(localSource, false);
 
 		    if (localBankCopy != null) {
 			boolean success = neighbour.setBank(localBankCopy, remoteDestination, true);
 			if (success) {
 			    return localBankCopy.getCost();
 			}
 		    }
 		} catch (IllegalArgumentException | SecurityException exc) {
 		    log.error("[transfer] Error instantiating Bank for transfer", exc);
 		}
 	    }
 
 	}
 	return 0;
     }
 
     private static int getNextSerialNumber() {
 	return lastSerial++;
     }
 
     /**
      * The main loop of the robot
      */
     @Override
     public void run() {
 	try {
 	    turnsControl.waitTurns(1); // block at the beginning so that all robots start at the
 				       // same time
 	    int oldRunningBank = 0;
 	    assert getData().isEnabled() : "Robot is disabled";
 	    while (alive) {
 		if (runningBank == 0) {
 		    if (banks[0] == null || banks[0].isEmpty()) {
 			die("Data Hunger");
 		    }
 		} else {
 		    if (runningBank > banks.length) {
 			if (runningBank >= GameSettings.getInstance().MAX_BANKS) {
 			    die("Impossible Bank number");
 			} else {
 			    reboot("Bank not found");
 			}
 		    } else if (banks[runningBank] == null || banks[runningBank].isEmpty()) {
 			// tried to execute empty bank, reboot
 			reboot("Tried to execute empty bank");
 		    }
 		}
 
 		// normal execution starts
 		if (alive) {
 		    if (runningBank != oldRunningBank
 			    && banks[runningBank].getTeamId() != banks[oldRunningBank].getTeamId()) {
 			eventDispatcher.fireEvent(new RobotChangedEvent(this));
 		    }
 		    oldRunningBank = runningBank;
 		    try {
 			banks[runningBank].run();
 		    } catch (BankInterruptedException exc) {
 			interrupted = false; // reset flag
 			log.debug("[run] Bank on {} was interrupted ", this);
 			pendingBankChange = true; // to start again on the new bank
 		    }
 
 		    if (!pendingBankChange && alive) {
 			reboot("End of bank");
 		    } else {
 			pendingBankChange = false;
 		    }
 		}
 
 	    }
 
 	    log.debug("[run] Robot terminated gracefully");
 	} catch (Exception | Error all) {
 	    log.error("[run] Problem running robot " + this, all);
 	    die("Execution Error -- " + all);
 	}
     }
 
     private void reboot(String reason) {
 	log.trace("[run] Rebooting robot: {}", reason);
 	changeBank(0);
     }
 
     @Override
     public void changeBank(int newBank) {
 	log.debug("[changeBank] Changing Bank of {}. Old bank = {}, new bank = {}", this, runningBank, newBank);
 	runningBank = newBank;
 	pendingBankChange = true; // so the robot doesn't reboot at the end of this bank
 
     }
 
     /**
      * Kills the robot and removes it from the board
      * 
      * @param reason user-friendly explanation for the death
      */
     @Override
     public void die(String reason) {
 	log.info("[die] Robot {} died with reason: {}", serialNumber, reason);
 	if (alive) { // if not alive it means it was killed at creation
 	    alive = false;
 	    interrupted = true; // to speed up death
 	    world.remove(Robot.this);
 	}
 
     }
 
     @Override
     public void die() {
 	die("No reason given");
 
     }
 
     /**
      * @return the total number of banks (including empty) in the robot
      */
     int getBanksCount() {
 	if (banks == null) {
 	    return 0;
 	}
 	return banks.length;
     }
 
     boolean setBank(Bank bank, int localBankIndex, boolean remoteInvoked) {
 	if (localBankIndex < 0 || localBankIndex >= banks.length) {
 	    if (!remoteInvoked) {
 		die("Invalid local bank position attempted");
 	    }
 	    return false;
 	} else {
 
 	    if (bank != null) {
 		bank.plugInterfaces(new RobotControlProxy(this), new RobotStatusProxy(this, world),
 			new WorldPlayerProxy(turnsControl, world));
 		if (localBankIndex == runningBank && alive && banks[localBankIndex] != null) {
 		    log.debug("[setBank] Changed running bank of {}", this);
 		    interrupted = true;
 		}
 		banks[localBankIndex] = bank;
 	    } else {
 		banks[localBankIndex] = null;
 	    }
 	    return true;
 	}
     }
 
     private Bank getBankCopy(int localBankIndex, boolean remoteInvoked) {
 	if (banks == null) {
 	    throw new IllegalArgumentException("Bank doesn't exist");
 	}
 	if (localBankIndex >= banks.length || localBankIndex < 0) {
 	    if (!remoteInvoked) {
 		die("Invalid local bank position");
 	    }
 	} else {
 	    if (banks[localBankIndex] != null) {
 		try {
 		    return banks[localBankIndex].getClass().getDeclaredConstructor(int.class)
 			    .newInstance(banks[localBankIndex].getTeamId());
 		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
 			| InvocationTargetException | NoSuchMethodException | SecurityException exc) {
 		    log.error("[getBankCopy] Error instantiating copy of Bank to transfer", exc);
 
 		}
 	    }
 	}
 	return null;
     }
 
     /**
      * Controls waiting of I/O
      * 
      * @author hilikus
      * 
      */
     public class TurnManager {
 
 	private int turnsCounter = 0;
 
 	private Delayer delayer;
 
 	private final Object syncObj = new Object();
 
 	/**
 	 * @param pClock master clock used to schedule tasks
 	 */
 	public TurnManager(Delayer pClock) {
 	    delayer = pClock;
 	}
 
 	/**
 	 * Blocks the robot
 	 * 
 	 * @param turns the number of turns to block
 	 * @throws BankInterruptedException if the execution is interrupted while waiting
 	 */
 	public void waitTurns(int turns) throws BankInterruptedException {
	    checkIfInterrupt();
 	    blockIfDisabled();
 	    if (turnsCounter > GameSettings.getInstance().MAX_AGE) {
 		die("Old Age");
 	    } else {
 		delayer.waitFor(serialNumber, turns);
 		turnsCounter += turns;
		checkIfInterrupt();
 		blockIfDisabled();
 	    }
 
 	}
 
 	private void checkIfInterrupt() throws BankInterruptedException {
 	    if (interrupted) {
 		throw new BankInterruptedException();
 	    }
 
 	}
 
 	private void blockIfDisabled() {
 	    synchronized (syncObj) {
 		while (!data.isEnabled()) {
 		    try {
 			syncObj.wait();
 		    } catch (InterruptedException exc) {
 			log.error("[waitTurns]", exc);
 		    }
 		}
 	    }
 	}
 
 	/**
 	 * @return the number of turns so far
 	 */
 	public int getTurnsCount() {
 	    return turnsCounter;
 	}
 
 	private void activated() {
 	    synchronized (syncObj) {
 		syncObj.notifyAll();
 	    }
 
 	}
     }
 
     /**
      * @return true if the robot is still alive; false otherwise
      */
     public boolean isAlive() {
 	return alive;
     }
 
     TurnManager getTurnsControl() {
 	return turnsControl;
     }
 
     @Override
     public void turn(boolean right) {
 	if (right) {
 	    data.setFacing(data.getFacing().right());
 	} else {
 	    data.setFacing(data.getFacing().left());
 	}
 	eventDispatcher.fireEvent(new RobotChangedEvent(this));
     }
 
     @Override
     public void createRobot(String pName, InstructionSet pSet, int banksCount, boolean pMobile) {
 	if (banksCount <= 0) {
 	    throw new IllegalArgumentException("Banks cannot be negative");
 	}
 	if (data.getInstructionSet().isLessThan(InstructionSet.SUPER)) {
 	    die("Robot cannot create other robots");
 	} else {
 	    int robotsCount = world.getBotsCount(data.getTeamId(), false);
 	    if (data.getGeneration() < GameSettings.getInstance().MAX_GENERATION
 		    && robotsCount < GameSettings.getInstance().MAX_BOTS) {
 		Robot child = new Robot(pSet, banksCount, pMobile, this, pName);
 
 		child.setEventDispatcher(eventDispatcher);
 
 		if (world.add(this, child)) { // world does further verification so add is not
 					      // guaranteed yet
 		    // all verifications passed
 		    child.alive = true;
 		    Thread newThread = new Thread(Thread.currentThread().getThreadGroup(), child, "Bot-"
 			    + child.getSerialNumber());
 		    newThread.start(); // jumpstarts the robot
 		}
 	    }
 	}
     }
 
     @Override
     public void move() {
 	if (!data.isMobile()) {
 	    die("Robot is not mobile but tried to move");
 	} else {
 	    world.move(this);
 	}
     }
 
     @Override
     public ScanResult scan() {
 	return scan(1);
     }
 
     @Override
     public ScanResult scan(int maxDist) {
 	if (maxDist < 1) {
 	    throw new IllegalArgumentException("Scan argument has to be positive");
 	}
 
 	if (data.getInstructionSet().isLessThan(InstructionSet.ADVANCED)) {
 	    die("Robot doesn't have Scan operation in its Instruction Set");
 	} else {
 
 	    ScanResult res = null;
 	    for (int dist = 1; dist <= maxDist; dist++) {
 		res = world.scan(this, dist);
 		if (!res.isEmpty()) {
 		    break;
 		}
 	    }
 	    assert res != null : "Scan result can't be null";
 
 	    return res;
 	}
 	return null;
     }
 
     /**
      * @return the unique ID of the robot
      */
     public int getSerialNumber() {
 	return serialNumber;
     }
 
     /**
      * @return an object with the state information
      */
     public RobotStatusLocal getData() {
 	return data;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
 	final int prime = 31;
 	int result = 1;
 	result = prime * result + serialNumber;
 	return result;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
 	if (this == obj) {
 	    return true;
 	}
 	if (obj == null || getClass() != obj.getClass()) {
 	    return false;
 	}
 
 	Robot other = (Robot) obj;
 	return serialNumber == other.serialNumber;
     }
 
     /**
      * @return the name of this robot
      */
     public String getName() {
 	return name;
     }
 
     /**
      * @return the owner
      */
     public Player getOwner() {
 	return owner;
     }
 
     /**
      * @return the id of the creator of the running bank
      */
     public int getRunningBankTeamId() {
 	if (banks[runningBank] != null) {
 	    return banks[runningBank].getTeamId();
 	} else {
 	    return data.getTeamId(); // nothing is running, return robot's team id
 	}
     }
 
     /**
      * called when a robot has been activated externally
      */
     void activated() {
 	turnsControl.activated();
 
     }
 
     @Override
     public void setEventDispatcher(EventDispatcher dispatcher) {
 	eventDispatcher = dispatcher;
 	data.setEventDispatcher(dispatcher);
     }
 
     EventDispatcher getEventDispatcher() {
 	return eventDispatcher;
     }
 
 }
