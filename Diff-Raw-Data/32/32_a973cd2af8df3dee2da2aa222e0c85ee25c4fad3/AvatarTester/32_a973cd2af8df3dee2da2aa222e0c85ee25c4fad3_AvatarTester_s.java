 package nine.graphics;
 
 import nine.graphics.Avatar;
 
 public class AvatarTester {
 	private int unitCircleX;
 	private int unitCircleY;
 	private int unitCircleWidth;
 	private int unitCircleHeight;
 	private int unitPostX;
 	private int unitPostY;
 	private int unitPostWidth;
 	private int unitPostHeight;
 	private int baseCircleX;
 	private int baseCircleY;
 	private int basePostX;
 	private int basePostY;
 	private int baseCircleWidth;
 	private int baseCircleHeight;
 	private int basePostWidth;
 	private int basePostHeight;
 	
 	public void test (Avatar avatar) {
 		Avatar baseAvatar = new Avatar(225, 225, 0, 30, 50, 50);
 		int testCase = this.compareAvatars(avatar, baseAvatar);
 		switch (testCase){
 			case 1: System.out.println("CASE 1 : Not touching: X locations"); break;
 			case 2: System.out.println("CASE 2 : Not touching: Y locations"); break;
 			case 3: System.out.println("CASE 3 : Circle width changed"); break;
 			case 4: System.out.println("CASE 4 : Relative distances not equal"); break;
 			default: System.out.println("Everything is good"); break;
 		}
 	}
 	
 	public int compareAvatars(Avatar one, Avatar two) {
		this.setUnitCircleX(one.getCircle().getLocation().getX());
		this.setUnitCircleY(one.getCircle().getLocation().getY());
		this.setUnitCircleWidth(one.getCircle().getWidth());
		this.setUnitCircleHeight(one.getCircle().getHeight());
		this.setUnitPostX(one.getPost().getLocation().getX());
		this.setUnitPostY(one.getPost().getLocation().getY());
		this.setUnitPostWidth(one.getPost().getWidth());
		this.setUnitPostHeight(one.getPost().getHeight());
		this.setBaseCircleX(two.getCircle().getLocation().getX());
		this.setBaseCircleY(two.getCircle().getLocation().getY());
		this.setBaseCircleWidth(two.getCircle().getWidth());
		this.setBaseCircleHeight(two.getCircle().getHeight());
		this.setBasePostX(two.getPost().getLocation().getX());
		this.setBasePostY(two.getPost().getLocation().getY());
		this.setBasePostWidth(two.getPost().getWidth());
		this.setBasePostHeight(two.getPost().getHeight());
 
 		//compare post touching circle X | Y
 		if (unitPostX != unitCircleX+(unitCircleWidth/2)) {return 1;};
 		if (unitPostY != unitCircleY+unitCircleHeight) {return 2;};
 		//check circle width
 		if (unitPostX != unitCircleX+(unitCircleWidth/2)) {return 3;};
 		//check relative distances
 		//d = sqrt ((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
 		double unitDist = (Math.sqrt(((unitPostX - unitCircleX)*(unitPostX - unitCircleX) + (unitPostY - unitCircleY)*(unitPostY - unitCircleY))));
 		double baseDist = (Math.sqrt(((basePostX - baseCircleX)*(basePostX - baseCircleX) + (basePostY - baseCircleY)*(basePostY - baseCircleY)))); 
 		if ( unitDist != baseDist ){return 4;};
 		//else everything is good
 		return 0; //can also return any negative number, or any non case integer
 	}
 	
 	public int getUnitCircleX() {
 		return this.unitCircleX;
 	}
 	public void setUnitCircleX(int circleX) {
 		this.unitCircleX = circleX;
 	}
 	public int getUnitCircleY() {
 		return this.unitCircleY;
 	}
 	public void setUnitCircleY(int circleY) {
 		this.unitCircleY = circleY;
 	}
 	public int getUnitPostX() {
 		return this.unitPostX;
 	}
 	public void setUnitPostX(int postX) {
 		this.unitPostX = postX;
 	}
 	public int getUnitPostY() {
 		return this.unitPostY;
 	}
 	public void setUnitPostY(int postY) {
 		this.unitPostY = postY;
 	}
 	public int getBaseCircleX() {
 		return this.baseCircleX;
 	}
 	public void setBaseCircleX(int baseCircleX) {
 		this.baseCircleX = baseCircleX;
 	}
 	public int getBaseCircleY() {
 		return this.baseCircleY;
 	}
 	public void setBaseCircleY(int baseCircleY) {
 		this.baseCircleY = baseCircleY;
 	}
 	public int getBasePostX() {
 		return this.basePostX;
 	}
 	public void setBasePostX(int basePostX) {
 		this.basePostX = basePostX;
 	}
 	public int getBasePostY() {
 		return this.basePostY;
 	}
 	public void setBasePostY(int basePostY) {
 		this.basePostY = basePostY;
 	}
 
 	public int getUnitCircleWidth() {
 		return unitCircleWidth;
 	}
 
 	public void setUnitCircleWidth(int unitCircleWidth) {
 		this.unitCircleWidth = unitCircleWidth;
 	}
 
 	public int getUnitCircleHeight() {
 		return unitCircleHeight;
 	}
 
 	public void setUnitCircleHeight(int unitCircleHeight) {
 		this.unitCircleHeight = unitCircleHeight;
 	}
 
 	public int getUnitPostWidth() {
 		return unitPostWidth;
 	}
 
 	public void setUnitPostWidth(int unitPostWidth) {
 		this.unitPostWidth = unitPostWidth;
 	}
 
 	public int getUnitPostHeight() {
 		return unitPostHeight;
 	}
 
 	public void setUnitPostHeight(int unitPostHeight) {
 		this.unitPostHeight = unitPostHeight;
 	}
 
 	public int getBaseCircleWidth() {
 		return baseCircleWidth;
 	}
 
 	public void setBaseCircleWidth(int baseCircleWidth) {
 		this.baseCircleWidth = baseCircleWidth;
 	}
 
 	public int getBaseCircleHeight() {
 		return baseCircleHeight;
 	}
 
 	public void setBaseCircleHeight(int baseCircleHeight) {
 		this.baseCircleHeight = baseCircleHeight;
 	}
 
 	public int getBasePostWidth() {
 		return basePostWidth;
 	}
 
 	public void setBasePostWidth(int basePostWidth) {
 		this.basePostWidth = basePostWidth;
 	}
 
 	public int getBasePostHeight() {
 		return basePostHeight;
 	}
 
 	public void setBasePostHeight(int basePostHeight) {
 		this.basePostHeight = basePostHeight;
 	}
 }
