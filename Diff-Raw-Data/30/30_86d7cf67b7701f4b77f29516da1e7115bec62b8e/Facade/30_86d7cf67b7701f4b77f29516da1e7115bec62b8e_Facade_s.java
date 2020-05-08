 package roborally.model;
 
 import java.io.Writer;
 import java.util.Set;
 
 import roborally.IFacade;
 import roborally.property.*;
 import roborally.utils.TargetNotReachableException;
 
 
 public class Facade implements IFacade<Board, Robot, Wall, Battery, RepairKit, SurpriseBox>{
 
 	@Override
 	public Board createBoard(long width, long height) {
 		return new Board(width,height);
 	}
 
 	@Override
 	public void merge(Board board1, Board board2) {
 		// TODO: Methode afwerken en aanpassen aan de nieuwe normen 
 
 	}
 
 	@Override
 	public Battery createBattery(double initialEnergy, int weight) {
 		return new Battery(new Energy(initialEnergy),new Weight(weight));
 	}
 
 	@Override
 	public void putBattery(Board board, long x, long y, Battery battery) {
		board.putEntity(new Position (x , y), battery);
 	}
 
 	@Override
 	public long getBatteryX(Battery battery) throws IllegalStateException {
 		return battery.getPosition().getX();
 	}
 
 	@Override
 	public long getBatteryY(Battery battery) throws IllegalStateException {
 		return battery.getPosition().getY();
 	}
 
 	@Override
 	public RepairKit createRepairKit(double repairAmount, int weight) {
 		return new RepairKit(new Energy(repairAmount), new Weight(weight));
 	}
 
 	@Override
 	public void putRepairKit(Board board, long x, long y, RepairKit repairKit) {
		board.putEntity(new Position(x, y), repairKit);
	}
 
 	@Override
 	public long getRepairKitX(RepairKit repairKit) throws IllegalStateException {
 		return repairKit.getPosition().getX();
 	}
 
 	@Override
 	public long getRepairKitY(RepairKit repairKit) throws IllegalStateException {
 		return repairKit.getPosition().getY();
 	}
 
 	@Override
 	public SurpriseBox createSurpriseBox(int weight) {
 		return new SurpriseBox(new Weight(weight));
 	}
 
 	@Override
 	public void putSurpriseBox(Board board, long x, long y,
 			SurpriseBox surpriseBox) {
 		board.putEntity(new Position(x, y), surpriseBox);
 	}
 
 	@Override
 	public long getSurpriseBoxX(SurpriseBox surpriseBox)
 			throws IllegalStateException {
 		return surpriseBox.getPosition().getX();
 
 	}
 
 	@Override
 	public long getSurpriseBoxY(SurpriseBox surpriseBox)
 			throws IllegalStateException {
 		return surpriseBox.getPosition().getY();
 	}
 
 	public static Orientation getOrientationEnum(int i ){
 		int tmp = Math.abs(i % 4);
 		if(i < 0){
 			if(tmp == 1){
 				return Orientation.LEFT;
 			}else if(tmp == 3){
 				return Orientation.RIGHT;
 			}
 		}
 		switch(tmp){
 		case 0:
 			return Orientation.UP;
 		case 1:
 			return Orientation.RIGHT;
 		case 2:
 			return Orientation.DOWN;
 		case 3:
 			return Orientation.LEFT;
 		}
 		return null;
 	}
 
 	@Override
 	public Robot createRobot(int orientation, double initialEnergy) {
 		return new Robot (getOrientationEnum(orientation),new Energy(initialEnergy));
 	}
 
 	@Override
 	public void putRobot(Board board, long x, long y, Robot robot) {
		board.putEntity(new Position(x, y), robot);
 	}
 
 	@Override
 	public long getRobotX(Robot robot) throws IllegalStateException {
 		return robot.getPosition().getX();
 	}
 
 	@Override
 	public long getRobotY(Robot robot) throws IllegalStateException {
 		return robot.getPosition().getY();
 	}
 
 	@Override
 	public int getOrientation(Robot robot) {
 		Orientation or = robot.getOrientation();
 		return or.ordinal();
 	}
 
 	@Override
 	public double getEnergy(Robot robot) {
 		return robot.getEnergy().getEnergy();
 	}
 
 	@Override
 	public void move(Robot robot) {
 		try{
 			robot.move();
 		}catch (IllegalStateException esc){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void turn(Robot robot) {
 		robot.turnClockWise();
 	}
 
 	@Override
 	public void pickUpBattery(Robot robot, Battery battery) {
 		try{
 			robot.pickUp(battery);
 		}catch(IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}catch(IllegalStateException esc){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void useBattery(Robot robot, Battery battery) {
 		try{
 			robot.use(battery);
 		}catch(IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void dropBattery(Robot robot, Battery battery) {
 		try{
 			robot.drop(battery);
 		}catch (IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}catch (IllegalStateException esc ){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void pickUpRepairKit(Robot robot, RepairKit repairKit) {
 		try{
 			robot.pickUp(repairKit);
 		}catch(IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}catch(IllegalStateException esc){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void useRepairKit(Robot robot, RepairKit repairKit) {
 		try{
 			robot.use(repairKit);
 		}catch(IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}	
 	}
 
 	@Override
 	public void dropRepairKit(Robot robot, RepairKit repairKit) {
 		try{
 			robot.drop(repairKit);
 		}catch (IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}catch (IllegalStateException esc ){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void pickUpSurpriseBox(Robot robot, SurpriseBox surpriseBox) {
 		try{
 			robot.pickUp(surpriseBox);
 		}catch(IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}catch(IllegalStateException esc){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void useSurpriseBox(Robot robot, SurpriseBox surpriseBox) {
 		try{
 			robot.use(surpriseBox);
 		}catch(IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void dropSurpriseBox(Robot robot, SurpriseBox surpriseBox) {
 		try{
 			robot.drop(surpriseBox);
 		}catch (IllegalArgumentException esc){
 			System.err.println(esc.getMessage());
 		}catch (IllegalStateException esc ){
 			System.err.println(esc.getMessage());
 		}
 	}
 
 	@Override
 	public void transferItems(Robot from, Robot to) {
 		// TODO: nieuwe methode? moet nog voll gemaakt worden. limiet van gewicht?
 
 	}
 
 	@Override
 	public int isMinimalCostToReach17Plus() {
 		// TODO: Aanpassen in de methode op mijn manier? via nieuwe exceptions zie heironder. Hoe hier implementeren? 
 		return 0;
 	}
 
 	@Override
 	public double getMinimalCostToReach(Robot robot, long x, long y) {
 		try {
 			return robot.getEnergyRequiredToReach(new Position(x, y)).getEnergy();
 		}catch (TargetNotReachableException esc){
 			System.err.println(esc.getMessage());
 			return -1;
 		}
 	}
 
 	@Override
 	public int isMoveNextTo18Plus() {
 		// TODO: geen idee hoe we dees juist moeten impl.
 		return 0;
 	}
 
 	@Override
 	public void moveNextTo(Robot robot, Robot other) {
 		robot.moveNextTo(other);
 	}
 
 	@Override
 	public void shoot(Robot robot) throws UnsupportedOperationException {
 		robot.shoot();
 	}
 
 	@Override
 	public Wall createWall() throws UnsupportedOperationException {
 		return new Wall();
 	}
 
 	@Override
 	public void putWall(Board board, long x, long y, Wall wall)
 			throws UnsupportedOperationException {
		board.putEntity(new Position(x, y), wall);
 	}
 
 	@Override
 	public long getWallX(Wall wall) throws IllegalStateException,
 	UnsupportedOperationException {
 		return wall.getPosition().getX();
 	}
 
 	@Override
 	public long getWallY(Wall wall) throws IllegalStateException,
 	UnsupportedOperationException {
 		return wall.getPosition().getY();
 	}
 
 	@Override
 	public Set<Robot> getRobots(Board board) {
 		return board.getRobots();
 	}
 
 	@Override
 	public Set<Wall> getWalls(Board board) throws UnsupportedOperationException {
 		return board.getWalls();
 	}
 
 	@Override
 	public Set<RepairKit> getRepairKits(Board board) {
 		return board.getRepairkits();
 	}
 
 	@Override
 	public Set<SurpriseBox> getSurpriseBoxes(Board board) {
 		return board.getSurpriseBoxes();
 	}
 
 	@Override
 	public Set<Battery> getBatteries(Board board) {
 		return board.getBatteries();
 	}
 
 	@Override
 	public int loadProgramFromFile(Robot robot, String path) {
 		// TODO: programma's beginnen maken.
 		return 0;
 	}
 
 	@Override
 	public int saveProgramToFile(Robot robot, String path) {
 		// TODO: programma's beginnen maken.
 		return 0;
 	}
 
 	@Override
 	public void prettyPrintProgram(Robot robot, Writer writer) {
 		// TODO: programma's beginnen maken.
 
 	}
 
 	@Override
 	public void stepn(Robot robot, int n) {
 		// TODO: programma's beginnen maken.
 
 	}
 
 }
