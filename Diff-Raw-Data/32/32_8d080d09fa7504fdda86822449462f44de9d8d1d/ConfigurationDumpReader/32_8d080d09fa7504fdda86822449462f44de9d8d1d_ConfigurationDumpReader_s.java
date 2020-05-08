 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import littlesmarttool2.comm.SerialCommand;
 import littlesmarttool2.model.Block;
 import littlesmarttool2.model.CameraBrand;
 import littlesmarttool2.model.CameraModel;
 import littlesmarttool2.model.Channel;
 import littlesmarttool2.model.Command;
 import littlesmarttool2.model.Configuration;
 import littlesmarttool2.model.ConnectionType;
 import littlesmarttool2.model.IRCommand;
 import littlesmarttool2.model.LANCCommand;
 import littlesmarttool2.model.ModelUtil;
 import littlesmarttool2.model.Setting;
 import littlesmarttool2.model.Threshold;
 import littlesmarttool2.model.WireCommand;
 
 /**
  *
  * @author Rasmus
  */
 public class ConfigurationDumpReader {
     private static final String testString = "V;3;0;2;2013;5;27;10;34;1\n" + "W;40;10;50;8;100\n" + "O;1\n" + "K;38\n" + "P;1000\n" + "L;1;0;0\n" + "L;2;0;0\n" + "L;3;0;0\n" + "L;4;0;0\n" + "L;5;0;0\n" + "L;6;0;0\n" + "L;7;0;0\n" + "L;8;0;0\n" + "L;9;0;0\n" + "L;10;0;0\n" + "L;11;0;0\n" + "L;12;0;0\n" + "L;13;0;0\n" + "L;14;0;0\n" + "L;15;0;0\n" + "L;16;0;0\n" + "L;17;0;0\n" + "L;18;0;0\n" + "L;19;0;0\n" + "L;20;0;0\n" + "L;21;0;0\n" + "L;22;0;0\n" + "L;23;0;0\n" + "L;24;0;0\n" + "L;25;0;0\n" + "L;26;0;0\n" + "L;27;0;0\n" + "L;28;0;0\n" + "L;29;0;0\n" + "L;30;0;0\n" + "L;31;0;0\n" + "L;32;0;0\n" + "L;33;0;0\n" + "L;34;0;0\n" + "L;35;0;0\n" + "L;36;0;0\n" + "L;37;0;0\n" + "L;38;0;0\n" + "L;39;0;0\n" + "L;40;0;0\n" + "L;41;0;0\n" + "L;42;0;0\n" + "L;43;0;0\n" + "L;44;0;0\n" + "L;45;0;0\n" + "L;46;0;0\n" + "L;47;0;0\n" + "L;48;0;0\n" + "L;49;0;0\n" + "L;50;0;0\n" + "R;1;4;1342;896;500;500;1;1\n" + "R;2;4;2232;1876;500;500;1;2\n" + "R;3;0;0;0;0;0;0;0\n" + "R;4;0;0;0;0;0;0;0\n" + "R;5;0;0;0;0;0;0;0\n" + "R;6;0;0;0;0;0;0;0\n" + "R;7;0;0;0;0;0;0;0\n" + "R;8;0;0;0;0;0;0;0\n" + "R;9;0;0;0;0;0;0;0\n" + "R;10;0;0;0;0;0;0;0\n" + "R;11;0;0;0;0;0;0;0\n" + "R;12;0;0;0;0;0;0;0\n" + "R;13;0;0;0;0;0;0;0\n" + "R;14;0;0;0;0;0;0;0\n" + "R;15;0;0;0;0;0;0;0\n" + "R;16;0;0;0;0;0;0;0\n" + "R;17;0;0;0;0;0;0;0\n" + "R;18;0;0;0;0;0;0;0\n" + "R;19;0;0;0;0;0;0;0\n" + "R;20;0;0;0;0;0;0;0\n" + "R;21;0;0;0;0;0;0;0\n" + "R;22;0;0;0;0;0;0;0\n" + "R;23;0;0;0;0;0;0;0\n" + "R;24;0;0;0;0;0;0;0\n" + "R;25;0;0;0;0;0;0;0\n" + "R;26;0;0;0;0;0;0;0\n" + "R;27;0;0;0;0;0;0;0\n" + "R;28;0;0;0;0;0;0;0\n" + "R;29;0;0;0;0;0;0;0\n" + "R;30;0;0;0;0;0;0;0\n" + "R;31;0;0;0;0;0;0;0\n" + "R;32;0;0;0;0;0;0;0\n" + "R;33;0;0;0;0;0;0;0\n" + "R;34;0;0;0;0;0;0;0\n" + "R;35;0;0;0;0;0;0;0\n" + "R;36;0;0;0;0;0;0;0\n" + "R;37;0;0;0;0;0;0;0\n" + "R;38;0;0;0;0;0;0;0\n" + "R;39;0;0;0;0;0;0;0\n" + "R;40;0;0;0;0;0;0;0\n" + "T;1;0;0;0;0;0;0\n" + "T;2;0;0;0;0;0;0\n" + "T;3;0;0;0;0;0;0\n" + "T;4;0;0;0;0;0;0\n" + "T;5;0;0;0;0;0;0\n" + "T;6;0;0;0;0;0;0\n" + "T;7;0;0;0;0;0;0\n" + "T;8;0;0;0;0;0;0\n" + "T;9;0;0;0;0;0;0\n" + "T;10;0;0;0;0;0;0\n" + "G;1;5;10000\n" + "G;2;5;10000\n" + "G;3;1;50\n" + "G;4;1;50\n" + "G;5;1;50\n" + "G;6;1;50\n" + "G;7;1;50\n" + "G;8;1;50\n" + "I;1;1;2400;600\n" + "I;1;2;1200;600\n" + "I;1;3;1200;600\n" + "I;1;4;600;600\n" + "I;1;5;1200;600\n" + "I;1;6;1200;600\n" + "I;1;7;600;600\n" + "I;1;8;600;600\n" + "I;1;9;1200;600\n" + "I;1;10;600;600\n" + "I;1;11;600;600\n" + "I;1;12;1200;600\n" + "I;1;13;1200;600\n" + "I;1;14;600;600\n" + "I;1;15;1200;600\n" + "I;1;16;1200;600\n" + "I;2;1;2400;600\n" + "I;2;2;600;600\n" + "I;2;3;1200;600\n" + "I;2;4;600;600\n" + "I;2;5;1200;600\n" + "I;2;6;1200;600\n" + "I;2;7;600;600\n" + "I;2;8;600;600\n" + "I;2;9;1200;600\n" + "I;2;10;600;600\n" + "I;2;11;600;600\n" + "I;2;12;1200;600\n" + "I;2;13;1200;600\n" + "I;2;14;600;600\n" + "I;2;15;1200;600\n" + "I;2;16;1200;600\n" + "D;1";
     
     private static final int MERGE_DISTANCE_LIMIT = 5; //Combined limit in servo values and promille (they are almost the same res.)
     
     private ConnectionType connectionType;
     private HashMap<Integer,LANCCommand> rawLANCCommands;
     private HashMap<Integer,IRCommand> rawIRCommands;
     private HashMap<Integer,WireCommand> rawWireCommands;
     private ArrayList<RangeProto> rangeProtos;
     private ArrayList<ThresholdProto> thresholdProtos;
     
     public static void main(String[] args)
     {
         //Load dump from string
         String[] cmds = testString.split("\n");
         SerialCommand[] testDump = new SerialCommand[cmds.length];
         for (int i = 0; i < cmds.length; i++)
         {
             testDump[i] = SerialCommand.fromMessage(cmds[i]);
         }
         
         //Set up dummy configuration
         Configuration conf = new Configuration();
         Channel ch4 = conf.getChannels().get(3);
         ch4.setCalibHigh(2232);
         ch4.setCalibLow(896);
         
         //DO IT
         ArrayList<CameraModel> possibleCameraModels = LoadDumpInto(conf, testDump);
         
         //Test
         System.out.println("Thresholds on ch 4: " + ch4.getSetting().getThresholds().size());
         System.out.println("Blocks on ch 4: " + ch4.getSetting().getBlocks().size());
         
         for (CameraModel m : possibleCameraModels)
         {
             System.out.println("Possible model: " + m.getIdentifier());
         }
     }
     
     /**
      * Loads a SS2 dump into a given configuration (must be calibrated)
      * @param configuration The calibrated configuration to load into
      * @param dump The SS2 dump to load
      * @return A list of possible camera models
      */
     public static ArrayList<CameraModel> LoadDumpInto(Configuration configuration, SerialCommand[] dump)
     {
         ConfigurationDumpReader instance = Read(dump);
         instance.SupplyCorrectCommands(LANCCommand.getArray(),IRCommand.getArray(),WireCommand.getArray());
         for (Channel ch : configuration.getChannels())
         {
             ch.setSetting(instance.Convert(ch.getId(), ch.getCalibLow(), ch.getCalibHigh()));
         }
         configuration.setOutputType(instance.connectionType);
 
         return instance.GetPossibleCameraModels(CameraBrand.getArray());
     }
     
     //4
     private ArrayList<CameraModel> GetPossibleCameraModels(CameraBrand[] cameraBrands)
     {
         ArrayList<CameraModel> possibleModels = new ArrayList<>();
         
         for (CameraBrand brand : cameraBrands)
         {
             for (CameraModel model : brand.getModels())
             {
                 if (IsPossibleCameraModel(model))
                     possibleModels.add(model);
             }
         }
         
         return possibleModels;
     }
     
     private boolean IsPossibleCameraModel(CameraModel model)
     {
         switch (connectionType)
         {
             case IR:
                 return model.getIRCommands().containsAll(rawIRCommands.values());
             case LANC:
                 return model.getLANCCommands().containsAll(rawLANCCommands.values());
             case Wire:
                 return model.getWireCommands().containsAll(rawWireCommands.values());
         }
         return false;
     }
             
     //3
     private Setting Convert(int channel, int calibLow, int calibHigh)
     {
         ArrayList<ThresholdProtoMerged> merged = GetMergedThresholdProtos(channel);
         ArrayList<Threshold> thresholds = CreateThresholdsFromMergedProtos(merged, calibLow, calibHigh);
         AddThresholdsFromRanges(channel, thresholds, calibLow, calibHigh);
         OrderThresholds(thresholds);
         ArrayList<Block> blocks = CreateBlocks(channel, thresholds, calibLow, calibHigh);
         
         return new Setting(blocks, thresholds);
     }
     
     private void AddThresholdsFromRanges(int channel, ArrayList<Threshold> thresholds, int calibLow, int calibHigh)
     {
         Command def = Command.getNothingCommand();
         for (RangeProto rp : rangeProtos)
         {
             if (rp.ch != channel) continue;
             if (ShouldCreateThesholdAt(thresholds,rp.max,calibLow,calibHigh))
                 thresholds.add(new Threshold(valueToPromille(rp.max, calibLow, calibHigh), def, def));
             if (ShouldCreateThesholdAt(thresholds,rp.min,calibLow,calibHigh))
                 thresholds.add(new Threshold(valueToPromille(rp.min, calibLow, calibHigh), def, def));
         }
     }
     
     private void OrderThresholds(ArrayList<Threshold> thresholds)
     {
         Collections.sort(thresholds, new Comparator<Threshold>() {
             @Override
             public int compare(Threshold o1, Threshold o2) {
                 return o1.getValuePromille() - o2.getValuePromille();
             }
         });
     }
     
     private ArrayList<Block> CreateBlocks(int channel, ArrayList<Threshold> thresholds, int calibLow, int calibHigh)
     {
         ArrayList<Block> blocks = new ArrayList<>();
         Threshold prevThreshold = null;
         for (Threshold th : thresholds)
         {
             int startPromille = prevThreshold == null ? 0 : prevThreshold.getValuePromille();
             RangeProto rp = GetRangeProtoStartingAt(channel, startPromille, calibLow, calibHigh);
             Command cmd = rp == null ? Command.getNothingCommand() : CommandFromID(rp.command);
             int interval = rp == null ? 0 : rp.tlow; //thigh = tlow
             
             blocks.add(new Block(cmd, prevThreshold, th, interval));
             prevThreshold = th;
         }
         //Add rightmost block
         int startPromille = prevThreshold == null ? 0 : prevThreshold.getValuePromille();
         RangeProto rp = GetRangeProtoStartingAt(channel, startPromille, calibLow, calibHigh);
         Command cmd = rp == null ? Command.getNothingCommand() : CommandFromID(rp.command);
         int interval = rp == null ? 0 : rp.tlow; //thigh = tlow
         blocks.add(new Block(cmd, prevThreshold, null, interval));
         
         return blocks;
     }
     
     private RangeProto GetRangeProtoStartingAt(int channel, int promillePoint, int calibLow, int calibHigh)
     {
         for (RangeProto rp : rangeProtos)
         {
             if (rp.ch != channel) continue;
             if (Math.abs(promillePoint-valueToPromille(rp.min, calibLow, calibHigh)) < MERGE_DISTANCE_LIMIT) return rp;
         }
         return null;
     }
     
     private boolean ShouldCreateThesholdAt(ArrayList<Threshold> thresholds, int point, int calibLow, int calibHigh)
     {
         for (Threshold th : thresholds)
         {
             if (Math.abs(th.getValuePromille()-valueToPromille(point, calibLow, calibHigh)) < MERGE_DISTANCE_LIMIT)
                 return false;
             if (Math.abs(point-calibLow) < MERGE_DISTANCE_LIMIT)
                 return false;
             if (Math.abs(point-calibHigh) < MERGE_DISTANCE_LIMIT)
                 return false;
         }
         return true;
     }
       
     private ArrayList<ThresholdProtoMerged> GetMergedThresholdProtos(int channel)
     {
         ArrayList<ThresholdProtoMerged> merged = new ArrayList<>();
         boolean[] visited = new boolean[thresholdProtos.size()+1];
         for (ThresholdProto tp : thresholdProtos)
         {
             if (visited[tp.id]) continue;
             if (tp.ch != channel) continue;
             visited[tp.id] = true;
             ThresholdProtoMerged tpm = new ThresholdProtoMerged();
             tpm.hyst = tp.hyst;
             tpm.id = tp.id;
             tpm.downcommand = (tp.glow == 1) ? tp.command : -1;
             tpm.upcommand = (tp.ghigh == 1) ? tp.command : -1;
             tpm.point = tp.point;
             for (ThresholdProto tp2 : thresholdProtos)
             {
                 if (visited[tp2.id]) continue;
                 if (tp2.ch != channel) continue;
                 if (tp2.point != tp.point) continue;
                 if (tp2.hyst != tp.hyst) continue;
                 tpm.downcommand = (tp2.glow == 1) ? tp2.command : tpm.downcommand;
                 tpm.upcommand = (tp2.ghigh == 1) ? tp2.command : tpm.upcommand;
                 visited[tp2.id] = true;
             }
             merged.add(tpm);
         }
         return merged;
     }
     
     private int valueToPromille(int value, int calibLow, int calibHigh)
     {
         int range = calibHigh - calibLow;
         return (value - calibLow) * 1000 / range;
     }
     
     private ArrayList<Threshold> CreateThresholdsFromMergedProtos(ArrayList<ThresholdProtoMerged> merged, int calibLow, int calibHigh)
     {
         ArrayList<Threshold> thresholds = new ArrayList<>();
         for (ThresholdProtoMerged tpm : merged)
         {
             thresholds.add(new Threshold(
                     valueToPromille(tpm.point, calibLow, calibHigh),
                     CommandFromID(tpm.upcommand),
                     CommandFromID(tpm.downcommand)));
         }
         return thresholds;
     }
     
     private Command CommandFromID(int id)
     {
         if (id == -1) return Command.getNothingCommand();
         switch (connectionType)
         {
             case IR: return rawIRCommands.get(id);
             case LANC: return rawLANCCommands.get(id);
             case Wire: return rawWireCommands.get(id);
             default: return Command.getNothingCommand();
         }
         
     }
     
     //1
     private static ConfigurationDumpReader Read(SerialCommand[] dump)
     {
         HashMap<Character,ArrayList<SerialCommand>> raw = BuildSearchMap(dump);
         ConfigurationDumpReader instance = new ConfigurationDumpReader();
         instance.connectionType = ReadConnectionType(raw);
         instance.rawLANCCommands = ReadLANCCommands(raw);
         instance.rawIRCommands = ReadIRCommands(raw, ReadIRFreq(raw));
         instance.rawWireCommands = GenerateWireCommands(ReadWirePulseWidth(raw));
         instance.rangeProtos = ReadRangeTriggers(raw);
         instance.thresholdProtos = ReadThresholdTriggers(raw);
         return instance;
     }
     
     //2
     private void SupplyCorrectCommands(LANCCommand[] LANCs, IRCommand[] IRs, WireCommand[] Wires)
     {
         SupplyCorrectCommands(LANCs, rawLANCCommands);
         SupplyCorrectCommands(IRs, rawIRCommands);
         SupplyCorrectCommands(Wires, rawWireCommands);
     }
     
     private <T extends Command> void SupplyCorrectCommands(T[] newCommands, HashMap<Integer, T> destCommands)
     {
         for (Integer key : destCommands.keySet())
         {
             Command oldCommand = destCommands.get(key);
             for (Command newCommand : newCommands)
             {
                 if (!oldCommand.sameAs(newCommand)) continue;
                 //Match
                 destCommands.put(key, (T) newCommand); //Safe cast as T is the same in new and dest
                 break;
             }
         }
     }
 
     private static HashMap<Character,ArrayList<SerialCommand>> BuildSearchMap(SerialCommand[] dump)
     {
         HashMap<Character,ArrayList<SerialCommand>> rawReadings = new HashMap<>();
         for (SerialCommand cmd : dump)
         {
             if (!rawReadings.containsKey(cmd.getCommand()))
                 rawReadings.put(cmd.getCommand(), new ArrayList<SerialCommand>());
             
             rawReadings.get(cmd.getCommand()).add(cmd);
         }
         return rawReadings;
     }
     
     private static ConnectionType ReadConnectionType(HashMap<Character,ArrayList<SerialCommand>> raw)
     {
         ConnectionType connectionType;
         switch (raw.get('O').get(0).convertArgsToInt()[0])
         {
             case 1: connectionType = ConnectionType.IR; break;
             case 2: connectionType = ConnectionType.Wire; break;
             case 3: connectionType = ConnectionType.LANC; break;
             default: throw new IllegalArgumentException("Unknown value of O");
         }
         return connectionType;
     }
     
     private static int ReadIRFreq(HashMap<Character,ArrayList<SerialCommand>> raw)
     {
         return raw.get('K').get(0).convertArgsToInt()[0];
     }
     
     private static int ReadWirePulseWidth(HashMap<Character,ArrayList<SerialCommand>> raw)
     {
         return raw.get('P').get(0).convertArgsToInt()[0];
     }
     
     private static HashMap<Integer, LANCCommand> ReadLANCCommands(HashMap<Character,ArrayList<SerialCommand>> raw)
     {
         HashMap<Integer,LANCCommand> cmds = new HashMap<>();
         for (SerialCommand scmd : raw.get('L'))
         {
             int[] parts = scmd.convertArgsToInt();
             if (parts[1] == 0 && parts[2] == 0) continue;
             cmds.put(parts[0], new LANCCommand("", "", null, parts[1], parts[2]));
         }
         
         return cmds;
     }
     
     private static HashMap<Integer, IRCommand> ReadIRCommands(HashMap<Character,ArrayList<SerialCommand>> raw, int freq)
     {
         HashMap<Integer, IRCommand> cmds = new HashMap<>();
         
         for (SerialCommand sc : raw.get('G'))
         {
             int[] parts = sc.convertArgsToInt();
             int[] pulseData = GetIRPulseData(raw,parts[0]);
             if (pulseData.length == 0) continue;
            cmds.put(parts[0], new IRCommand("","",null,pulseData,parts[2],parts[1],freq));
         }
         
         return cmds;
     }
     
     private static int[] GetIRPulseData(HashMap<Character,ArrayList<SerialCommand>> raw, int commandId)
     {
         HashMap<Integer, int[]> pulseParts = new HashMap<>();
         if (!raw.containsKey('I')) return new int[0];
         for (SerialCommand cmd : raw.get('I'))
         {
             int[] parts = cmd.convertArgsToInt();
             if (parts[0] != commandId) continue;
             pulseParts.put(parts[1], new int[]{parts[2],parts[3]});
         }
         int[] pulseData = new int[pulseParts.size()*2];
         int j = 0;
         for (int i = 1; i <= pulseParts.size(); i++)
         {
             pulseData[j] = pulseParts.get(i)[0];
             pulseData[j+1] = pulseParts.get(i)[1];
             j += 2;
         }
         return pulseData;
     }
     
     private static HashMap<Integer, WireCommand> GenerateWireCommands(int pulseLength)
     {
         HashMap<Integer, WireCommand> commands = new HashMap<>();
         commands.put(1, new WireCommand("Focus", "", null, pulseLength, 1));
         commands.put(2, new WireCommand("Shoot", "", null, pulseLength, 2));
         commands.put(3, new WireCommand("Fo+Sh", "", null, pulseLength, 3));
         return commands;
     }
     
     private static class RangeProto
     {
         public int id, ch, max, min, thigh, tlow, expo, command;
     }
     
     private static ArrayList<RangeProto> ReadRangeTriggers(HashMap<Character,ArrayList<SerialCommand>> raw)
     {
         ArrayList<RangeProto> ranges = new ArrayList<>();
         for (SerialCommand cmd : raw.get('R'))
         {
             int[] parts = cmd.convertArgsToInt();
             RangeProto range = new RangeProto();
             range.id = parts[0];
             range.ch = parts[1];
             range.max = parts[2];
             range.min = parts[3];
             range.thigh = parts[4];
             range.tlow = parts[5];
             range.expo = parts[6];
             range.command = parts[7];
             ranges.add(range);
         }
         return ranges;
     }
     
     private static class ThresholdProtoMerged
     {
         public int id, point, hyst, upcommand, downcommand;
     }
     
     private static class ThresholdProto
     {
         public int id, ch, point, ghigh, glow, hyst, command;
     }
     
     private static ArrayList<ThresholdProto> ReadThresholdTriggers(HashMap<Character,ArrayList<SerialCommand>> raw)
     {
         ArrayList<ThresholdProto> thresholds = new ArrayList<>();
         for (SerialCommand cmd : raw.get('T'))
         {
             int[] parts = cmd.convertArgsToInt();
             ThresholdProto threshold = new ThresholdProto();
             threshold.id = parts[0];
             threshold.ch = parts[1];
             threshold.point = parts[2];
             threshold.ghigh = parts[3];
             threshold.glow = parts[4];
             threshold.hyst = parts[5];
             threshold.command = parts[6];
             thresholds.add(threshold);
         }
         return thresholds;
     }
     
 }
