 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.data_generator.generate.calls;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.amanzi.neo.data_generator.data.calls.CallData;
 import org.amanzi.neo.data_generator.data.calls.CallGroup;
 import org.amanzi.neo.data_generator.data.calls.CommandRow;
 import org.amanzi.neo.data_generator.data.calls.ProbeData;
 import org.amanzi.neo.data_generator.utils.call.CommandCreator;
 
 /**
  * <p>
  * Generator for GroupCallsData. 
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public class GroupCallsGenerator extends AmsDataGenerator{
     
    private static final float[] CALL_DURATION_BORDERS = new float[]{0.05f,0.125f,0.25f,0.375f,0.5f,0.75f,1,2,5,1000};
     private static final String PAIR_DIRECTORY_POSTFIX = "GroupCall";
     
     private int maxGroupSize;
 
     /**
      * @param aDirectory
      * @param aHours
      * @param aHourDrift
      * @param aCallsPerHour
      * @param aCallPerHourVariance
      * @param aMaxGroupSize
      * @param aProbes
      */
     public GroupCallsGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes, Integer aMaxGroupSize) {
         super(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
         maxGroupSize = aMaxGroupSize;
     }
 
     @Override
     protected CallData buildCall(CallGroup group, Integer hour, Long duration) {
         Long startTime = getStartTime(); 
         Long networkIdentity = getNetworkIdentity();
         List<ProbeInfo> probes = getProbes();
         Long startHour = startTime+HOUR*hour;
         Long endHour = startTime+HOUR*(hour+1);
         Long start = getRamdomTime(startHour, endHour);
         
         Integer sourceNum = group.getSourceProbe();
         List<Integer> receiverNums = group.getReceiverProbes();
         
         Long time = getRamdomTime(startHour, start);
         ProbeData source = getNewProbeData(time, sourceNum);
         ProbeInfo sourceInfo = probes.get(sourceNum-1);
         List<CommandRow> sourceCommands = source.getCommands();
         
         int resCount = receiverNums.size();
         ProbeData[] receivers = new ProbeData[resCount];
         List<ProbeInfo> allReceiversInfo = new ArrayList<ProbeInfo>(resCount);
         List<List<CommandRow>> allReceiverCommands = new ArrayList<List<CommandRow>>(resCount);
         for(int i=0; i<resCount; i++){
             Integer receiverNum = receiverNums.get(i);
             time = getRamdomTime(time, start);
             receivers[i] = getNewProbeData(time, receiverNum);
             allReceiversInfo.add(probes.get(receiverNum-1));
             allReceiverCommands.add(receivers[i].getCommands());
         }
         
         time = getRamdomTime(time, start);
         sourceCommands.add(CommandCreator.getAtCtgsRow(time));
         CommandRow sourceCtgs = CommandCreator.getCtgsRow(sourceInfo.getSourceGroups(),sourceInfo.getResGroups());
         sourceCommands.add(CommandCreator.getAtCtgsRow(time,sourceCtgs));
         
         for(int i=0; i<resCount; i++){
             List<CommandRow> receiverCommands = allReceiverCommands.get(i);
             ProbeInfo resInfo = allReceiversInfo.get(i);
             time = getRamdomTime(time, start);
             receiverCommands.add(CommandCreator.getAtCtgsRow(time));
             CommandRow receiverCtgs = CommandCreator.getCtgsRow(resInfo.getSourceGroups(),resInfo.getResGroups());
             receiverCommands.add(CommandCreator.getAtCtgsRow(time,receiverCtgs));
         }
         
         time = getRamdomTime(time, start);
         sourceCommands.add(CommandCreator.getAtCciRow(time));
         CommandRow sourceCci = CommandCreator.getCciRow(networkIdentity,sourceInfo.getLocalAria(),sourceInfo.getFrequency());
         sourceCommands.add(CommandCreator.getAtCciRow(time,sourceCci));
         
         for(int i=0; i<resCount; i++){
             List<CommandRow> receiverCommands = allReceiverCommands.get(i);
             ProbeInfo receiverInfo = allReceiversInfo.get(i);
             time = getRamdomTime(time, start);
             receiverCommands.add(CommandCreator.getAtCciRow(time));
             CommandRow receiverCci = CommandCreator.getCciRow(networkIdentity,receiverInfo.getLocalAria(),receiverInfo.getFrequency());
             receiverCommands.add(CommandCreator.getAtCciRow(time,receiverCci));
         }
         
         CommandRow ctsdcRow = CommandCreator.getCtsdcRow(start,0,0,0,1,1,0,1,1,0,0);
         sourceCommands.add(ctsdcRow);        
         time = getRamdomTime(0L, duration);
         sourceCommands.add(CommandCreator.getCtsdcRow(start+time,ctsdcRow));
         
         time = getRamdomTime(time, duration);
         CommandRow atdRow = CommandCreator.getAtdRow(start+time, group.getGroupNumber());
         sourceCommands.add(atdRow);
         
         Long end = start+duration;
         CommandRow ctcc1 = CommandCreator.getCtccRow(end, 2,1,1,0,0,1,1);
         
         Long rest = startTime+HOUR*(hour+1)-end;
         if(rest<10){
             rest = HOUR;
         }
         
         CommandRow ctcc2 = CommandCreator.getCtccRow(null, 2,1,1,0,0,1,1);
         String numKey = networkIdentity+"0"+sourceInfo.getPhoneNumber();
         CommandRow ctxg = CommandCreator.getCtxgRow(numKey,2,3,0,0,1);
         
         time = getRamdomTime(0L, rest);
         for(int i=0; i<resCount; i++){
             List<CommandRow> receiverCommands = allReceiverCommands.get(i);
             Long time1 = getRamdomTime(0L, time);
             receiverCommands.add(CommandCreator.getCticnRow(end+time1, numKey, ctcc2, ctxg));
         }
         
         sourceCommands.add(CommandCreator.getAtdRow(end+time, atdRow, ctcc1, CommandCreator.getCtxgRow(numKey,2,0,0,0)));
         
         time = getRamdomTime(0L, rest);
         sourceCommands.add(CommandCreator.getAthRow(end+time));
         
         time = getRamdomTime(time, rest);
         CommandRow ctcrRow = CommandCreator.getCtcrRow(end+time,2,1);
         time = getRamdomTime(time, rest);
         sourceCommands.add(CommandCreator.getAthRow(end+time,ctcrRow));
         
         for(int i=0; i<resCount; i++){
             List<CommandRow> receiverCommands = allReceiverCommands.get(i);
             time = getRamdomTime(time, rest);
             ctcrRow = CommandCreator.getCtcrRow(null,2,14);
             receiverCommands.add(CommandCreator.getUnsoCtcrRow(end+time,ctcrRow));
         }      
         
         return new CallData(getKey(),source, receivers);
     }
     
 
     @Override
     protected String getDirectoryPostfix() {
         return PAIR_DIRECTORY_POSTFIX;
     }
 
     @Override
     protected List<CallGroup> initCallGroups() {
         List<CallGroup> result = new ArrayList<CallGroup>();
         int groupSize = 1;
         while (groupSize<maxGroupSize) {
             groupSize++;            
             List<List<Integer>> groups = buildAllGroups(groupSize);
             for(List<Integer> group : groups){
                 boolean canBeGroup = getRandomGenerator().getBooleanValue();
                 if(canBeGroup){
                     int source = group.get(0);
                     CallGroup call = getCallGroup(source, group.subList(1, groupSize).toArray(new Integer[0]));
                     result.add(call);
                 }
             }
             
         }
         return result;
     }
 
     /**
      * Build all possible gropes.
      *
      * @param size int Group size
      * @return List<List<Integer>>
      */
     private List<List<Integer>> buildAllGroups(int size){
         Integer probesCount = getProbesCount();
         List<List<Integer>> result = new ArrayList<List<Integer>>();
         if(size == 1){
             for(int i=1;i<=probesCount; i++){
                 List<Integer> group = new ArrayList<Integer>(size);
                 group.add(i);
                 result.add(group);
             }
             return result;
         }
         List<List<Integer>> before = buildAllGroups(size-1);
         for(int i=1;i<=probesCount; i++){
             for(List<Integer> group : before){
                 if(!group.contains(i)){
                     List<Integer> newGroup = new ArrayList<Integer>(size);
                     newGroup.addAll(group);
                     newGroup.add(i);
                     result.add(newGroup);
                 }                
             }
         }
         return result;
     }
 
     @Override
     protected float[] getCallDurationBorders() {
         return CALL_DURATION_BORDERS;
     }
 }
