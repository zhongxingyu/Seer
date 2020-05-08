 package de.zaunkoenigweg.runningdb.domain;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 /**
  * Training log.
  * 
  * @author Nikolaus Winter
  */
 public class TrainingLog {
     
     /**
      * list of all trainings in log.
      */
     private List<Training> trainings = new ArrayList<Training>();
     
     /**
      * list of all shoes in log.
      */
     private List<Shoe> shoes = new ArrayList<Shoe>();
     
     /**
      * sorted set of all distances to be kept track of in the record list.
      */
     private SortedSet<RecordDistance> recordDistances = new TreeSet<RecordDistance>(new Comparator<RecordDistance>() {
         @Override
         public int compare(RecordDistance recordDistance1, RecordDistance recordDistance2) {
             return recordDistance1.getDistance().compareTo(recordDistance2.getDistance());
         }
     });
     
     public List<Training> getTrainings() {
         return new ArrayList<Training>(this.trainings);
     }
     
     public List<Shoe> getShoes() {
         return new ArrayList<Shoe>(this.shoes);
     }
     
     public List<Shoe> getActiveShoes() {
         List<Shoe> result = new ArrayList<Shoe>();
         for (Shoe shoe : this.shoes) {
             if(shoe.isActive()) {
                 result.add(shoe);
             }
         }
         return result;
     }
     
     public List<ShoeInfo> getShoeInfo() {
         List<ShoeInfo> result = new ArrayList<ShoeInfo>();
         for (Shoe shoe : this.shoes) {
             result.add(new ShoeInfo(shoe, getDistance(shoe)));
         }
         return result;
     }
     
     public void addShoe(Shoe shoe) {
         
         // TODO: ID existance check
         if(shoe.getId()!=0) {
             
             this.shoes.add(shoe);
             
         // new shoe gets next free ID
         } else {
             
             int maxId = 0;
             for (Shoe existingShoe : this.shoes) {
                 maxId = Math.max(maxId, existingShoe.getId());
             }
             
             shoe.setId(maxId+1);
             this.shoes.add(shoe);
 
         }
         
     }
     
     public Shoe getShoe(Integer id) {
         
         if(id==null) {
             return null;
         }
         
         Shoe result = null;
         
         List<Shoe> shoes = this.getShoes();
         int i=0;
         Shoe shoe;
         while(result==null && i<shoes.size()) {
             shoe = shoes.get(i++);
             if (id.compareTo(shoe.getId())==0) {
                 result=shoe;
             }
         }
         
         return result;
         
     }
     
     public void addTraining(Training training) {
 
         this.trainings.add(training);
         
     }
     
     public List<RecordDistance> getRecordDistances() {
         return new ArrayList<RecordDistance>(this.recordDistances);
     }
     
     public void addRecordDistance(RecordDistance recordDistance) {
         this.recordDistances.add(recordDistance);
     }
     
     public void removeRecordDistance(RecordDistance recordDistance) {
         this.recordDistances.remove(recordDistance);
     }
     
     
     // ------------------------------------------------------------------------------
     // Reporting
     // ------------------------------------------------------------------------------
     
     /**
      * Returns sum of distance of all runs of all trainings.
      * 
      * @return sum of distance
      */
     public Integer getDistance() {
         int distance = 0;
         for (Training training: this.trainings) {
             distance += training.getDistance();
         }
         return distance;
     }
     
     /**
      * Returns sum of distance of all runs of all trainings for given shoe.
      * 
      * @param shoe running shoe
      * @return sum of distance
      */
     public Integer getDistance(Shoe shoe) {
         int distance = 0;
         for (Training training: this.trainings) {
             if(training.getShoe()!=null && training.getShoe().compareTo(shoe.getId())==0) {
                 distance += training.getDistance();
             }
         }
         return distance;
     }
     
     /**
      * Returns sum of elapsed time of all runs of all trainings.
      * 
      * @return sum of elapsed time
      */
     public Integer getTime() {
         
         int time = 0;
         for (Training training: this.trainings) {
             time += training.getTime();
         }
         return time;
     }
     
     /**
      * Returns list of all training sessions in given period.
      * @param year Year as specified by {@link Calendar}
      * @param month Month as specified by {@link Calendar}
      * @return list of all training sessions in given period
      */
     public List<Training> getTrainings(int year, int month) {
         List<Training> result = new ArrayList<Training>();
         
         for (Training training : this.trainings) {
             
             Calendar period = Calendar.getInstance();
             period.setTime(training.getDate());
             
             if(period.get(Calendar.YEAR)==year && period.get(Calendar.MONTH)==month) {
                 result.add(training);
             }
             
         }
         
         return result;
     }
     
     /**
      * Returns list of informations regarding records.
      * @return list of informations regarding records
      */
     public List<RecordInfo> getRecords() {
         
         List<RecordInfo> result = new ArrayList<RecordInfo>();
         
         for (RecordDistance recordDistance : this.recordDistances) {
             RecordInfo recordInfo = getRecordInfo(recordDistance);
             result.add(recordInfo);
         }
         
         return result;
         
     }
 
     /**
      * Returns record info for given record distance
      * @param recordDistance record distance
      * @return record info
      */
     private RecordInfo getRecordInfo(RecordDistance recordDistance) {
         
         // Caution! This algorithm is not suitable for big data ;-)
         
         final RecordInfo recordInfo = new RecordInfo(recordDistance);
         
         // get all runs with matching distance 
         for (Training training : this.getTrainings()) {
             for (Run run : training.getRuns()) {
                 if(run.getDistance().compareTo(recordDistance.getDistance())==0) {
                     recordInfo.getRecordRuns().add(new RecordInfo.RecordRun(run.getTime(), training));
                 }
             }
         }
         
         // sort runs (fastest first)
         Collections.sort(recordInfo.getRecordRuns(), new Comparator<RecordInfo.RecordRun>() {
 
             @Override
             public int compare(RecordInfo.RecordRun run1, RecordInfo.RecordRun run2) {
                 return run1.getTime().compareTo(run2.getTime());
             }
         });
         
         // limit runs to 10
         while(recordInfo.getRecordRuns().size()>10) {
             recordInfo.getRecordRuns().remove(10);
         }
 
         return recordInfo;
     }
     
     /**
      * Generates report containing all training sessions, grouped and sorted by year.
      */
     public TrainingReport generateTraininigReport() {
         
         TrainingReport report = new TrainingReport();
         
         SortedMap<Integer, Map<String, Integer>> data = new TreeMap<Integer, Map<String, Integer>>();
         
         Calendar trainingDate = null;
         Integer year;
         Integer countAll = 0;
         Integer totalDistance = 0;
         Integer totalTime = 0;
         for (Training training : this.getTrainings()) {
             
             trainingDate = Calendar.getInstance();
             trainingDate.setTime(training.getDate());
             year = trainingDate.get(Calendar.YEAR);
             
             if(!data.containsKey(year)) {
                 data.put(year, new HashMap<String, Integer>());
                 data.get(year).put("distance", Integer.valueOf(0));
                 data.get(year).put("time", Integer.valueOf(0));
                 data.get(year).put("number", Integer.valueOf(0));
             }
             
             data.get(year).put("distance", data.get(year).get("distance") + training.getDistance());
             totalDistance += training.getDistance();
             data.get(year).put("time", data.get(year).get("time") + training.getTime());
             totalTime += training.getTime();
             data.get(year).put("number", data.get(year).get("number") + 1);
             countAll ++;
             
         }
         
         Map<String, Integer> row = null;
         for (Integer year2: data.keySet()) {
             row = data.get(year2);
             report.getReportRows().add(new TrainingReport.TrainingReportRow(year2, null, row.get("number"), row.get("distance"), row.get("time")));
         }
         report.setSumRow(new TrainingReport.TrainingReportRow(null, null, countAll, totalDistance, totalTime));
         
         return report;
         
     }
     
 }
