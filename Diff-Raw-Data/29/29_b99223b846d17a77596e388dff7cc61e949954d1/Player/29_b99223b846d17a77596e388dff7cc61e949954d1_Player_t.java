 package nl.javadude.monopoly.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  */
 @SuppressWarnings("serial")
 public class Player implements Serializable, MoneyExchanger {
 	private String name;
 	private ISquare currentPosition;
 	private long money;
 	private List<IOwnable> possessions = new ArrayList<IOwnable>();
 	private final Board board;
     private boolean jailed;
     private boolean active;
 
     public Player(Board board, String name) {
 		this.name = name;
 		this.board = board;
 		// Add the base money to the player.
         Bank.BANK.pay(1500, this);
 		setCurrentPosition(board.START);
 	}
 
 	public void setCurrentPosition(ISquare currentPosition) {
 		this.currentPosition = currentPosition;
 	}
 
 	public ISquare getCurrentPosition() {
 		return currentPosition;
 	}
 
 	public boolean pay(long amount, MoneyExchanger toPlayer) {
        long amountToPay = Math.min(amount, money);
		toPlayer.receiveMoney(amountToPay);
		money -= amountToPay;
         return true;
 	}
 
 	public void receiveMoney(long amount) {
 		money += amount;
 	}
 
 	public long getMoney() {
 		return money;
 	}
 
 	public void setMoney(long money) {
 		this.money = money;
 	}
 
 	public List<IOwnable> getPossessions() {
 		return possessions;
 	}
 
 	public void addPossession(IOwnable ownable) {
 		this.possessions.add(ownable);
 	}
 
 	public boolean isJailed() {
 		return jailed;
 	}
 
     public boolean isActive() {
         return active;
     }
 
     public boolean buy() {
 		if (currentPosition instanceof IOwnable) {
 			IOwnable ownable = (IOwnable) currentPosition;
 			if (ownable.forSale()) {
				if (canPayFor(ownable))
                 {
                    pay(ownable.getCost(), Bank.BANK);
 				    ownable.setOwned(this);
 				    addPossession(ownable);
 				    return true;
                 }
 			}
 		}
 		return false;
 	}
 
    private boolean canPayFor(IOwnable ownable) {
        return ownable.getCost() <= money;
    }

    void payRent() {
 		if (currentPosition instanceof IOwnable) {
 			IOwnable ownable = (IOwnable) currentPosition;
 			if (!ownable.isUnowned()) {
 				if (!pay(ownable.getRent(), ownable.owner()))
                 {
                     pay(ownable.owner().getMoney(), ownable.owner());
                 }
 			}
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null || !(obj instanceof Player)) {
 			return false;
 		}
 		Player that = (Player) obj;
 		return (that.getName() != null && that.getName().equals(getName()));
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean owns(String name) {
 		ISquare sq = board.findLocation(name);
 		return possessions.contains(sq);
 	}
 
 
     public void activate() {
         this.active = true;
     }
 
     public void deactivate() {
         this.active = false;
     }
 }
