 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class Player {
     private final int MAX_TOO_NUMBER = 10;
     private String name;
     private int money;
     private Site site;
     private static final String[] names = new String[]{"QianFuRen", "ATuBo", "SunXiaoMei", "JinBeiBei"};
     private boolean isBroke = false;
     private int points;
     private List<Tool> tools = new ArrayList<Tool>();
     private List<Property> properties = new ArrayList<Property>();
     private GodOfLuck godOfLuck;
     private boolean isInjured;
     private boolean inPrison;
     private boolean isActive;
     private Scanner scanner;
 
     public Player(String name, Site site, int money) {
         this.name = name;
         this.money = money;
         if (site != null) setPlayerOnSite(site);
 
         scanner = new Scanner(System.in);
         // TODO
         isActive = true;
     }
 
     public static Player createPlayer(char playerChar, Site site, int money) {
         int index = Integer.valueOf(String.valueOf(playerChar));
         return new Player(names[index - 1], site, money);
     }
 
     public static int playerSize() {
         return names.length;
     }
 
     public void takeTurn() {
         becomeActive();
         RollCommand roll = waiting();
         moving(roll);
         stopping();
     }
 
     private void becomeActive() {
         isActive = true;
     }
 
     private void moving(RollCommand roll) {
         executeCommand(roll);
     }
 
     private RollCommand waiting() {
         while (true) {
             String string = scanner.next();
             Command command = Command.makeCommand(string, this);
             if (command instanceof RollCommand) return (RollCommand) command;
             executeCommand(command);
         }
     }
 
     public void forward(int steps) {
         int remainSteps = steps;
         while (remainSteps-- > 0) {
             forwardSingleStep();
             if (isBlocked()) break;
         }
     }
 
     public void stopping() {
         while (isActive()) {
             Command command = site.giveCommand(this);
             executeCommand(command);
         }
     }
 
     public void sentToHospital() {
         isActive = false;
         isInjured = true;
         setSite(site.findNearestHospital());
 
         registerInjured();
     }
 
     private void registerInjured() {
         RoundEngine.instance().registerImmovablePlayer(this, HospitalSite.ROUND_TO_STAY_FOR_INJURED_PLAYER);
     }
 
     private boolean isBlocked() {
         return site.hasBlockTool();
     }
 
     private void forwardSingleStep() {
         site.removePlayer(this);
         Site nextSite = site.nextSite();
         setSite(nextSite);
     }
 
     public String display() {
         return getFirstCharacterOfName();
     }
 
     private String getFirstCharacterOfName() {
         return String.valueOf(name.charAt(0)).toUpperCase();
     }
 
     public String getName() {
         return name;
     }
 
     public void setSite(Site targetSite) {
         setPlayerOnSite(targetSite);
     }
 
     private void registerPrisoned() {
         RoundEngine.instance().registerImmovablePlayer(this, PrisonSite.ROUND_TO_STAY_IN_PRISON);
     }
 
     private void setPlayerOnSite(Site targetSite) {
         if (targetSite instanceof PrisonSite) {
             registerPrisoned();
             setInPrison(true);
         }
 
         site = targetSite;
         targetSite.addPlayer(this);
     }
 
     public Site getSite() {
         return site;
     }
 
     public boolean isPropertyUpgradable(PropertyLevel propertyLevel) {
         return isAffordable(propertyLevel.getPrice()) && !(propertyLevel instanceof Skyscraper);
     }
 
     private void payMoney(int price) {
         money -= price;
     }
 
     private boolean isAffordable(int price) {
         return money >= price;
     }
 
     public int getMoney() {
         return money;
     }
 
     public void payMoneyToPlayer(int money, Player player) {
         payMoney(money);
         player.earnMoney(money);
     }
 
     public void broke() {
         isBroke = true;
     }
 
     public void earnMoney(int money) {
         this.money += money;
     }
 
     public boolean isBroke() {
         return isBroke;
     }
 
     public void setPoints(int points) {
         this.points = points;
     }
 
     public void buyTool(int toolCode) {
         Tool tool = Tool.makeTool(toolCode);
         if (!canAffordTool(tool)) throw new PointsException();
         if (tools.size() >= MAX_TOO_NUMBER) throw new ToolException();
         buyTool(tool);
     }
 
     public void buyTool(Tool tool) {
         payPoints(tool.getPoint());
         addTool(tool);
     }
 
     public boolean canAffordTool(Tool tool) {
        return points >= tool.getPoint();
     }
 
     public int getToolsNumber() {
         return tools.size();
     }
 
     public void addTool(Tool tool) {
         tools.add(tool);
     }
 
     public int getPoints() {
         return points;
     }
 
     public void sellTool(int toolCode) {
         Tool tool = findTool(toolCode);
         tools.remove(tool);
         earnPoints(tool.getPoint());
     }
 
     private void removeTool(Tool toolToRemove) {
         tools.remove(toolToRemove);
     }
 
     public int countTool(int toolCode) {
         int result = 0;
 
         for (Tool tool : tools) {
             if (tool.matchToolCode(toolCode)) result++;
         }
 
         return result;
     }
 
     public void setMoney(int money) {
         this.money = money;
     }
 
     public void earnPoints(int points) {
         this.points += points;
     }
 
     private void payPoints(int points) {
         this.points -= points;
     }
 
     public void setGodOfLuck(GodOfLuck godOfLuck) {
         this.godOfLuck = godOfLuck;
         if (hasGodOfLuck()) {
             registerGodOfLuck();
         }
     }
 
     private void registerGodOfLuck() {
         RoundEngine.instance().registerPlayerWithGodOfLuck(this, GodOfLuck.ROUND_IN_EFFECT);
     }
 
     public boolean hasGodOfLuck() {
         return godOfLuck != null;
     }
 
     public void upgradeProperty(Property property) {
         if (isPropertyUpgradable(property.getLevel())) {
             payMoney(property.getPrice());
             property.upgrade();
         }
     }
 
 
     private void sellProperty(Property property) {
         if (property.getOwner() == this) {
             earnMoney(property.getSalePrice());
             property.reset();
             properties.remove(property);
         }
     }
 
     public void buyProperty(Property property) {
         if (isAffordable(property.getPrice())) {
             payMoney(property.getPrice());
             property.setOwner(this);
         }
     }
 
     public void payTollFee(Property property) {
         Player propertyOwner = property.getOwner();
 
         int tollFee = property.getTollFee();
 
         if (!isAffordable(tollFee)) {
             tollFee = money;
             broke();
         }
 
         payMoneyToPlayer(tollFee, propertyOwner);
     }
 
     public void mine(PointMineSite pointMineSite) {
         earnPoints(pointMineSite.getPoint());
     }
 
     private Site getSiteToPlaceTool(int distance) {
         Site siteToPlaceTool = site;
 
         if (distance > 0) {
             while (distance-- > 0) {
                 siteToPlaceTool = siteToPlaceTool.nextSite();
             }
         } else if (distance < 0) {
             while (distance++ < 0) {
                 siteToPlaceTool = siteToPlaceTool.previousSite();
             }
         }
 
         return siteToPlaceTool;
     }
 
     public void useTool(int toolCode, int distance) {
         Tool tool = findTool(toolCode);
         Site siteToPlaceBombTool = getSiteToPlaceTool(distance);
 
         tool.usedOnSite(siteToPlaceBombTool);
         removeTool(tool);
     }
 
     private Tool findTool(int toolCode) {
         for (Tool tool : tools) {
             if (tool.matchToolCode(toolCode)) {
                 return tool;
             }
         }
 
         throw new ToolNotFoundException();
     }
 
     public void setInjured(boolean isInjured) {
         this.isInjured = isInjured;
     }
 
     public boolean isInjured() {
         return isInjured;
     }
 
     public void setMovable() {
         setInjured(false);
         setInPrison(false);
     }
 
     public boolean isMovable() {
         return !isInjured() && !isInPrison();
     }
 
     private boolean isInPrison() {
         return inPrison;
     }
 
 
     public void setInPrison(boolean inPrison) {
         this.inPrison = inPrison;
     }
 
     public void executeCommand(Command command) {
         if (command.isExecutable(this))
             command.execute();
     }
 
     public void sellPropertyByIndex(int siteIndex) {
         Property propertyToSell = findPropertyByIndex(siteIndex);
         sellProperty(propertyToSell);
     }
 
     private Property findPropertyByIndex(int siteIndex) {
         for (Property property : properties) {
             if (siteIndex == property.getIndex())
                 return property;
         }
         throw new PropertyNotFoundException();
     }
 
     public String query() {
         String format = "资金：%d 元\n" +
                 "点数：%d 点\n" +
                 "地产：空地 %d 处；茅屋 %d 处；洋房 %d 处；摩天楼 %d 处。\n" +
                 "道具：路障 %d 个；炸弹 %d 个；机器娃娃 %d 个\n";
         String result = String.format(format,
                 getMoney(),
                 getPoints(),
                 countProperty(Land.LAND_TYPE_CODE), countProperty(Cabin.CABIN_TYPE_CODE),
                 countProperty(House.HOUSE_TYPE_CODE), countProperty(Skyscraper.SKYSCRAPER_TYPE_CODE),
                 countTool(BlockTool.BLOCK_TOOL_CODE), countTool(BombTool.BOMB_TOOL_CODE),
                 countTool(RobotTool.ROBOT_TOOL_CODE));
 
         return result;
     }
 
     public int countProperty(String propertyType) {
         int result = 0;
 
         for (Property property : properties) {
             if (property.matchPropertyType(propertyType)) result++;
         }
 
         return result;
     }
 
     public void addProperty(Property property) {
         properties.add(property);
     }
 
     public void chooseGift(int giftCode) {
         Gift gift = Gift.makeGift(giftCode);
         openGift(gift);
     }
 
     private void openGift(Gift gift) {
         gift.open(this);
     }
 
     public boolean isActive() {
         return isActive;
     }
 
     public void becomeInactive() {
         isActive = false;
     }
 }
