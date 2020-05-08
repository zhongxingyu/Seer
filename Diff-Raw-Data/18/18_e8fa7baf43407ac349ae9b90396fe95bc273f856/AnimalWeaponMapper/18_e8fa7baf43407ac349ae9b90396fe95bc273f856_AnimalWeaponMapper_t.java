 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import java.util.*;
 
 /**
  *
  * @author Shreyas Valmiki
  */
 public final class AnimalWeaponMapper {
     private HashMap<Integer,ArrayList<Integer>> animalWeaponMap = new HashMap<Integer, ArrayList<Integer>>();
     private HashMap<Integer,Integer> animalToWeaponNo = new HashMap<Integer, Integer>();
     private HashMap<String,ArrayList<String>> huntMap = new HashMap<String, ArrayList<String>>();
     
     private Animals animals = new Animals();
     private Weapons weapons = new Weapons();
     
     private HashMap<Integer,String> animalList = new HashMap<Integer, String>();
     private HashMap<Integer,String> weaponList = new HashMap<Integer, String>();
     
     private ArrayList<Integer> indexInsertedList = new ArrayList<Integer>();
     private int animalListSize = 0;
     private int weaponListSize = 0;
 
     public AnimalWeaponMapper(){
         
     }
     AnimalWeaponMapper(int huntedWith2,int huntedWith3,int huntedWith4){
         initMap();   
         setAnimalToWeaponRatio(huntedWith2,huntedWith3,huntedWith4);
         computeAndSetHuntMap();
     }
     
     private void initMap(){
         animalList = animals.getAnimalList();
         weaponList = weapons.getWeaponList();
         
         weaponListSize = weaponList.size();
         animalListSize = animalList.size();
         
         for(Integer i: animalList.keySet()){
             ArrayList<Integer> list = new ArrayList<Integer>();
             
             for(int j=1;j<=weaponListSize;j++){
                 list.add(j);     
             }
             animalWeaponMap.put(i, list);
         }
     }
     
     private void setAnimalToWeaponRatio(int huntedWith2,int huntedWith3,int huntedWith4){
     	//test
     	//System.out.println("in setAnimalToWeaponRatio");
         int total = huntedWith2 + huntedWith3 + huntedWith4;
         
         
         if(total < animalListSize)
         {
             huntedWith3 += animalList.size() - total;
         }
         
         //test
     	//System.out.println("in setAnimalToWeaponRatio");
        fillRatioList(2,huntedWith2);
        fillRatioList(3,huntedWith3);
        fillRatioList(4,huntedWith4);
       //test
     	System.out.println("out setAnimalToWeaponRatio");
     }
     
    private void fillRatioList(int weaponToAnimalNo,int huntTimes){
     	 //test
     	//System.out.println("in fillRatioList");
         Random rand = new Random();
         for(int i = 0; i<huntTimes;i++){
             //int key = rand.nextInt(animalListSize)+1;
             boolean hasInserted = false;
             while(!hasInserted){
             	int key = rand.nextInt(animalListSize)+1;
                 if(!indexInsertedList.contains(key)){
                     indexInsertedList.add(key);
                    animalToWeaponNo.put(key, weaponToAnimalNo);
                     hasInserted = true;
                 }
             }
         }
         //test
     	//System.out.println("out fillRatioList");
     }
     
     private void computeAndSetHuntMap(){
     	//test
     	//System.out.println("in computeAndSetHuntMap");
     	
         ArrayList<Integer> rowSumList = new ArrayList<Integer>();
         HashMap<Integer,Integer> mandatoryList = new HashMap<Integer, Integer>();
         int rowSum;
         boolean isRowDone = false;
         Random rand = new Random();
         Random randMand = new Random();
         int ratio = 0;
         int randIndex;
         
         int count = 0;
         while(mandatoryList.size() < weaponListSize){
         	int randNo = randMand.nextInt(weaponListSize)+1;
         	if(!mandatoryList.keySet().contains(randNo)){
         		mandatoryList.put(randNo,++count);
         	}
         }
         for(int i: animalToWeaponNo.keySet()){
             ratio = animalToWeaponNo.get(i);
             ArrayList<Integer> randList = new ArrayList<Integer>();
             ArrayList<Integer> wList = new ArrayList<Integer>();
             wList = animalWeaponMap.get(i);
             isRowDone = false;
             rowSum = 0;
             while(!isRowDone){
                randIndex = rand.nextInt(weaponListSize) + 1; 
                //if(!randList.contains(randIndex) && (weaponListSize-randList.size()) < ratio && mandatoryList.get(i) != randIndex){
               if(!randList.contains(randIndex) && (weaponListSize-randList.size()) > ratio && mandatoryList.get(i) != randIndex){
                    randList.add(randIndex);
                    wList.set(wList.indexOf(randIndex), -1);
                }
               if((weaponListSize-randList.size()) == ratio){
             	   isRowDone = true;
                }
             }
         }
         setHuntMap();
         //test
         //System.out.println("in computeAndSetHuntMap");
     }
     
     private void setHuntMap(){
     	//test
     	//System.out.println("in setHuntMap");
         for(int key: animalWeaponMap.keySet()){
             ArrayList<String> huntWeaponNames = new ArrayList<String>();
             ArrayList<Integer> huntWeaponIndexes = new ArrayList<Integer>();
             huntWeaponIndexes = animalWeaponMap.get(key);
             for(int i: huntWeaponIndexes){
                 if(i == -1)
                 {
                     huntWeaponNames.add("");
                 }
                 else
                 {
                     huntWeaponNames.add(weaponList.get(i));
                 }
             }
             huntMap.put(animalList.get(key), huntWeaponNames);
         }
         //test
         //System.out.println("in setHuntMap");
     }
     
     public HashMap<Integer,ArrayList<Integer>> getAnimalWeaponMap(){
         return animalWeaponMap;
     }
     
     public HashMap<String,ArrayList<String>> getHuntMap(){
         return huntMap;
     }
 }
