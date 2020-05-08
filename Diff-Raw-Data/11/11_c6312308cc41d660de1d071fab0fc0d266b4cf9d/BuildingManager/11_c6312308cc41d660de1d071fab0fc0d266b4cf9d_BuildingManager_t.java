 package travianWindow;
 
 import Library.Building;
 import Library.BuildingLibrary;
 import Library.Resources;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.util.ArrayList;
 
 public class BuildingManager extends BuildingLibrary {
 
     private travianModel model;
     private ArrayList villagesList;
     public Village[] villages;
 
     public BuildingManager() {
         model = travian.model;
 
         villagesList = new ArrayList();
     }
 
     public void init() {
     }
 
     public void fetchMapFields(ArrayList<Element> dorf1, ArrayList<Element> dorf2) {
         dorf1.clear();
         dorf2.clear();
 
         // download dorf1
         if (model.web.url.indexOf("dorf1.php") == -1) { // not on dorf1 page
             model.web.reset(travian.baseURL + "dorf1.php");
             model.web.connect(false);
         }
 
         addToArrayList(dorf1);
 
         model.web.reset(travian.baseURL + "dorf2.php");
         model.web.connect(false);
 
         addToArrayList(dorf2);
 
     }
 
     public int getFieldLocation(String buildingName) {
         int l = -1;
 
         Village v = villages[model.getVillageIndex(model.overview.village_name.getText())];
 
         ArrayList<Element> map = (buildingName.equals("Woodcutter")
                 || buildingName.equals("Clay Pit")
                 || buildingName.equals("Iron Mine")
                 || buildingName.equals("Cropland")) ? v.dorf1 : v.dorf2;
 
         // resource tile
         for (Element a : map) {
             if (l != -1) { // already found location, break out of loop
                 break;
             }
 
             if (a.attr("alt").indexOf(buildingName) != -1) {
                 String fieldBuildingName = a.attr("alt").split(" Level ")[0];
 
                 if (buildingName.equals(fieldBuildingName)) {
                     l = Integer.parseInt(a.attr("href").split("=")[1]);
                     break;
                 }
             }
         }
 
         return l;
     }
 
     public int getBuildingLevel(int location) {
         int l = -1;
 
         Village v = villages[model.getVillageIndex(model.overview.village_name.getText())];
         ArrayList<Element> map = (location > 18) ? v.dorf2 : v.dorf1;
 
         for (Element a : map) {
             int mapId = Integer.valueOf(a.attr("href").split("=")[1]);
 
             if (mapId == location) {
                 String raw = a.attr("alt");
                 String buildingName = raw.split(" Level ")[0];
 
                 int villageBuildingLevelMax = 0;
                 for (int i = 0; i < v.currently_building; i++) {
                     if (v.buildingName[i].equals(buildingName)) {
                         int buildLevel = Integer.valueOf(v.buildingLevel[i].replaceAll("Level ", ""));
                         if (buildLevel > villageBuildingLevelMax) {
                             villageBuildingLevelMax = buildLevel;
                         }
                     }
                 }
                 for (int i = 0; i < model.queue.model.getRowCount(); i++) {
                     String bvName = model.queue.model.getValueAt(i, 0).toString();
                     String bName = model.queue.model.getValueAt(i, 1).toString();
 
                     if (bvName.equals(v.getName()) && bName.equals(buildingName)) {
                         // villages & building names match
                         int buildLevel = Integer.valueOf(model.queue.model.getValueAt(i, 2).toString());
                         if (buildLevel > villageBuildingLevelMax) {
                             villageBuildingLevelMax = buildLevel;
                         }
                     }
                 }
 
                 l = Integer.parseInt(raw.split(" Level ")[1]);
                 if (villageBuildingLevelMax > l) {
                     l = villageBuildingLevelMax;
                 }
 
                 break;
             }
         }
 
         return l;
     }
 
     private void addToArrayList(ArrayList<Element> al) {
         Elements el = model.web.parser.doc.getElementsByTag("map").first().getElementsByTag("area");
 
         for (int i = 0; i < el.size(); i++) {
             Element e = el.get(i);
             e.removeAttr("shape");
             e.removeAttr("coords");
             e.removeAttr("title"); // alt used instead
 
             if (e.attr("href").equals("dorf2.php")) {
                 continue;
             }
 
             al.add(e);
         }
     }
 
     public void addBuildingToQueue(Building b) {
         model.queue.addRow(b);
     }
 
     public synchronized void update() {
         // called every second
         model.overview.building1.setText("");
         model.overview.building2.setText("");
 
         for (Village v : villages) {
             v.update();
 
             if (v.refresh) {
                 model.refresh(v.getName());
 
                 v.refresh = false;
             }
 
             if (v.getName().equals(model.overview.village_name.getText())) {
                 String[] b = v.getBuildString();
 
                 if (b.length > 0) {
                     model.overview.building1.setText(b[0]);
                 }
                 if (b.length > 1) {
                     model.overview.building2.setText(b[1]);
                 }
             }
         }
 
         for (int i = 0; i < model.queue.buildings.size(); i++) {
             try {
                 Building b = model.queue.buildings.get(i);
                 b.update();
 
                 model.queue.model.setValueAt(b.getStartTime(), i, 3);
                 model.queue.model.setValueAt(b.getEndTime(), i, 4);
             } catch (Exception e) {
                 System.out.println("Exception " + e.getMessage() + " when trying to update buildingManager");
             }
         }
     }
 
     public boolean building() {
         // returns true upon building any building in any village
         for (Object o : villagesList) {
             if (getCurrentlyBuilding(o.toString()).length != 0) {
                 // length not zero, so queue is either 1 or 2.. building something, return true.
                 return true;
             }
         }
 
         // return false because nothing is being built
         return false;
     }
 
     public int getSuggestedTimeForTimeDelay() {
 
         int shortestTime = -1;
 
         for (Village v : villages) {
             int t = v.timeLeftSeconds();
             if (t != -1) {
                 if (t <= shortestTime || shortestTime == -1) {
                     shortestTime = t;
                 }
             }
 
         }
 
         if (model.queue.buildings.size() > 0) {
             Building b = model.queue.buildings.get(0); // first in queue building
 
             int rLumber = b.getLumber();
             int rClay = b.getClay();
             int rIron = b.getIron();
             int rWheat = b.getWheat();
 
             Resources r = model.resources[ model.getVillageIndex(model.overview.village_name.getText())];
 
             long t = r.getEstimate(rLumber, rClay, rIron, rWheat);
 
             if (t > 0) {
                 if ((shortestTime == -1 || shortestTime > t) && t != 0) {
                     shortestTime = (int) t;
                 }
             }
         }
 
         return shortestTime;
 
     }
 
     public void buildNextObject() {
         for (Village v : villages) {
             boolean building = false;
             
             if (v.currently_building != 0) {
                 building = true;
             }
 
             if (building) // cannot build next object if building another
             {
                 continue;
             }
 
             if (model.queue.buildings.size() > 0) {
                 Building b = model.queue.buildings.get(0); // first in queue building
                 
                 // if village-specific task
                 System.out.println (v.getName());
                 System.out.println (b.getVillage());
                 if (!b.getVillage().equals(v.getName())) continue;
                 
                 Resources r = model.resources[ model.getVillageIndex(model.overview.village_name.getText())];
 
                 long t = r.getEstimate(b.getLumber(), b.getClay(), b.getIron(), b.getWheat());
 
                 if (t <= 0) {
 
                     // build the object
                     model.web.reset(travian.baseURL + "build.php?id=" + b.getLocation());
                     model.web.connect(false);
                     System.out.println(model.web.parser.doc.html());
                     Element contract = model.web.parser.doc.getElementById("contract");
                     if (contract != null) {
                         Element button = contract.getElementsByClass("contractLink").first().getElementsByTag("button").first();
 
                         String href = button.attr("onclick");
                         href = href.substring(24, href.length()).replaceAll("'; return false;", "");
 
                         model.web.reset(travian.baseURL + href);
                         model.web.connect(false);
 
                         model.queue.removeRow(0);
                         model.refresh();
                     }
 
                 }
 
             }
         }
     }
 
     public long getStartTimeForNextBuilding(String village_name, Building b) {
         int length = 0;
         Resources r = model.resources[ model.getVillageIndex(village_name)];
         boolean building = false;
        
        int vLeft = 0;
         for (Village v : villages) {
             if (v.name.equals(village_name)) {
 
                 for (int i = 0; i < v.currently_building; i++) {
                    vLeft += v.timeLeftSeconds[i];
                 }
 
             }
         }
        length += vLeft;
 
         if (model.queue.model.getRowCount() > 0) {
             String[] lastBuild = model.queue.model.getValueAt(model.queue.model.getRowCount() - 1, 4).toString().split(":");
 
             int hrs = 0;
             if (lastBuild.length > 2) {
                 hrs = Integer.parseInt(lastBuild[0]);
             }
             int min = Integer.parseInt(lastBuild[lastBuild.length - 2]);
             int sec = Integer.parseInt(lastBuild[lastBuild.length - 1]);
 
             length += ((hrs * 60) * 60) + (min * 60) + sec;
         }
 
         // calculating all resources that will be used up
         int lu = 0,
                 cl = 0,
                 ir = 0,
                 wh = 0;
 
         for (int i = 0; i < model.queue.model.getRowCount(); i++) {
             building = true;
             lu += Integer.parseInt(model.queue.model.getValueAt(i, 5).toString());
             cl += Integer.parseInt(model.queue.model.getValueAt(i, 6).toString());
             ir += Integer.parseInt(model.queue.model.getValueAt(i, 7).toString());
             wh += Integer.parseInt(model.queue.model.getValueAt(i, 8).toString());
         }
 
         Object[] futureRes = r.getTimeEstimate(length);
 
         long t = 0;
 
         if (futureRes != null) {
             lu = Integer.valueOf(futureRes[0].toString()) - lu;
             cl = Integer.valueOf(futureRes[1].toString()) - cl;
             ir = Integer.valueOf(futureRes[2].toString()) - ir;
             wh = Integer.valueOf(futureRes[3].toString()) - wh;
         } else {
             lu = r.getLumber() - lu;
             cl = r.getClay() - cl;
             ir = r.getIron() - ir;
             wh = r.getWheat() - wh;
         }
 
         int tlu = r.getLumber(),
                 tcl = r.getClay(),
                 tir = r.getIron(),
                 twh = r.getWheat();
 
         if (building) {
             r.setLumberStock(lu);
             r.setClayStock(cl);
             r.setIronStock(ir);
             r.setWheatStock(wh);
 
             t = r.getEstimate(b.getLumber(), b.getClay(), b.getIron(), b.getWheat());
 
             r.setLumberStock(tlu);
             r.setClayStock(tcl);
             r.setIronStock(tir);
             r.setWheatStock(twh);
         } else {
             t = r.getEstimate(b.getLumber(), b.getClay(), b.getIron(), b.getWheat());
         }
 
 
        return t + vLeft;
     }
 
     public String[] getCurrentlyBuilding(String village) {
         // update
         int village_index = -1;
 
         for (int i = 0; i < villagesList.size(); i++) {
             if (villagesList.get(i).toString().equals(villages[i].getName())) {
                 village_index = i;
                 break;
             }
         }
 
         if (village_index == -1) {
             String[] t = new String[0];
             return t;
         }
 
         String[] s = villages[village_index].getBuildString();
 
         return s;
     }
 
     public void addVillage(Object[] v) {
         // manages buildings in all villages
         villagesList.add(v[1].toString());
         int i = Integer.parseInt(v[0].toString());
 
         villages[i] = new Village(v);
         villages[i].parseBuild();
     }
 
     public void setVillageCount(int i) {
         villages = new Village[i];
     }
 
     public class Village {
 
         /*
          * v[0] = village count
          * v[1] = village name
          */
         public int id;
         private String name;
         private String[] buildingName; // building name
         private String[] buildingLevel; // building to level x
         private String[] timeLeft; // time coutdown
         private String[] timeString; // time when finishes
         public int currently_building;
         private int[] timeLeftSeconds;
         private boolean refresh; // true refreshes village building.
         public Building main_building; // stores all buildings in this village
         public ArrayList<Element> dorf1;
         public ArrayList<Element> dorf2;
 
         public Village(Object[] v) {
             this.id = Integer.parseInt(v[0].toString());
             this.name = v[1].toString();
 
             dorf1 = new ArrayList();
             dorf2 = new ArrayList();
             fetchMapFields(dorf1, dorf2);
 
             main_building = new Building("Main Building", 26, Integer.parseInt(dorf2.get(7).attr("alt").split(" Level ")[1]));
         }
 
         public void update() {
             currently_building = 0;
 
             if (buildingName.length > 0) {
                 if (buildingName.length > 1 && buildingName[1] != null) {
                     currently_building = 2;
                 } else {
                     currently_building = 1;
                 }
                 if (buildingName[0] != null) {
                     currently_building = 1;
                 } else {
                     currently_building = 0;
                 }
             }
 
             for (int i = 0; i < currently_building; i++) {
                 timeLeftSeconds[i]--;
 
                 int t = timeLeftSeconds[i];
                 if (t <= 0) {
                     removeBuildingInQueue(i);
                 }
             }
         }
 
         public String getName() {
             return name;
         }
 
         public void removeBuildingInQueue(int i) {
             if (currently_building == 2) {
                 buildingName[0] = buildingName[1];
                 buildingLevel[0] = buildingName[1];
                 timeLeft[0] = timeLeft[1];
                 timeString[0] = timeLeft[1];
                 timeLeftSeconds[0] = timeLeftSeconds[1];
             }
 
             buildingName[i] = buildingLevel[i] = timeLeft[i] = timeString[i] = null;
             timeLeftSeconds[i] = 0;
 
             refresh = true; // set to refresh buildings.
         }
 
         public int timeLeftSeconds() {
             // returns shortest time left of building
             // -1 returns nothing being built
             if (timeLeftSeconds.length == 0) {
                 return -1;
             }
             return timeLeftSeconds[0];
         }
 
         public void parseBuild() {
             if (model.web.parser.doc.getElementById("building_contract") != null) {
                 Elements rows = model.web.parser.doc.getElementById("building_contract").getElementsByTag("tbody").first().getElementsByTag("tr");
 
                 buildingName = new String[rows.size()];
                 buildingLevel = new String[rows.size()];
                 timeLeft = new String[rows.size()];
                 timeString = new String[rows.size()];
                 timeLeftSeconds = new int[rows.size()];
 
                 currently_building = rows.size();
 
                 for (int i = 0; i < rows.size(); i++) {
                     if (i == 2) {
                         break; //cannot go over size of two
                     }
 
                     Element row = rows.get(i);
                     Element building = row.getElementsByTag("td").get(1); // second td contains building details
                     Element time = row.getElementsByTag("td").get(2); // second td contains building times
 
                     String rawBuildingText = building.text();
 
                     buildingLevel[i] = building.getElementsByTag("span").first().text();
                     buildingName[i] = rawBuildingText.replaceAll(" " + buildingLevel[i], "");
 
                     timeLeft[i] = time.getElementsByTag("span").first().text();
                     timeString[i] = time.text().replaceAll(timeLeft[i] + " hrs. done ", "");
 
                     int hours = Integer.parseInt(timeLeft[i].split(":")[0]);
                     int minutes = Integer.parseInt(timeLeft[i].split(":")[1]);
                     int seconds = Integer.parseInt(timeLeft[i].split(":")[2]);
 
                     timeLeftSeconds[i] = ((hours * 60) * 60) + (minutes * 60) + seconds;
                 }
             } else {
                 buildingName = new String[0];
                 buildingLevel = new String[0];
                 timeLeft = new String[0];
                 timeString = new String[0];
                 timeLeftSeconds = new int[0];
             }
         }
 
         public String[] getBuildString() {
             if (currently_building == 0) {
                 String[] s = new String[0];
                 return s;
             } // not building anything..
 
             String[] s = new String[currently_building]; // currently building, max two (excluding gold user's master builder
 
             for (int i = 0; i < currently_building; i++) {
                 int t = timeLeftSeconds[i];
 
                 String hrs;
                 String min;
                 String sec;
 
                 if (t >= (60 * 60)) {
                     int temp = (int) ((t / 60) / 60);
                     hrs = String.valueOf(temp);
                     if (temp < 10) {
                         hrs = "0" + temp;
                     }
 
                     t = t - ((temp * 60) * 60);
                 } else {
                     hrs = "00";
                 }
                 if (t >= 60) {
                     int temp_m = (int) (t / 60);
                     int temp_s = (int) (t - (temp_m * 60));
 
                     min = String.valueOf(temp_m);
                     if (temp_m < 10) {
                         min = "0" + temp_m;
                     }
 
                     sec = String.valueOf(temp_s);
                     if (temp_s < 10) {
                         sec = "0" + temp_s;
                     }
                 } else {
                     min = "00";
 
                     sec = String.valueOf(t);
                     if (t < 10) {
                         sec = "0" + t;
                     }
                 }
 
                 s[i] = String.format("- %s (%s)\t%s %s", buildingName[i], buildingLevel[i], hrs + ":" + min + ":" + sec, timeString[i]);
             }
 
             return s;
         }
     }
 }
