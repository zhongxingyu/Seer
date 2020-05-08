 package ch.k42.metropolis.model.parcel;
 
 import ch.k42.metropolis.WorldEdit.Clipboard;
 import ch.k42.metropolis.WorldEdit.ClipboardProvider;
 import ch.k42.metropolis.generator.MetropolisGenerator;
 import ch.k42.metropolis.minions.Constants;
 import ch.k42.metropolis.minions.GridRandom;
 import ch.k42.metropolis.model.enums.ContextType;
 import ch.k42.metropolis.model.enums.Direction;
 import ch.k42.metropolis.model.enums.RoadType;
 import ch.k42.metropolis.model.grid.Grid;
 import org.bukkit.Chunk;
 
 import java.util.List;
 
 /**
  * This Parcel represents a Road in Metropolis
  * @author Thomas Richner
  */
 public class RoadParcel extends StreetParcel {
 
     private static final int chunkSizeX = 1;
     private static final int chunkSizeZ = 1;
 
     public RoadParcel(Grid grid,int chunkX,int chunkZ) {
         super(grid,chunkX,chunkZ,chunkSizeX,chunkSizeZ, ContextType.STREET);
         grid.fillParcels(chunkX,chunkZ,this);
     }
 
     private Clipboard road;
 
     @Override
     public void populate(MetropolisGenerator generator, Chunk chunk) {
         if(chunk.getX()==(chunkX)&&chunk.getZ()==(chunkZ)){
 
             // determine street needed
 
             ContextType context = grid.getParcel(chunkX+1,chunkZ).getContextType();
             boolean hasEast = context.equals(ContextType.STREET) || context.equals(ContextType.HIGHWAY);
             context = grid.getParcel(chunkX-1,chunkZ).getContextType();
             boolean hasWest = context.equals(ContextType.STREET) || context.equals(ContextType.HIGHWAY);
             context =grid.getParcel(chunkX,chunkZ-1).getContextType();
             boolean hasNorth = context.equals(ContextType.STREET) || context.equals(ContextType.HIGHWAY);
             context = grid.getParcel(chunkX,chunkZ+1).getContextType();
             boolean hasSouth = context.equals(ContextType.STREET) || context.equals(ContextType.HIGHWAY);
 
             ClipboardProvider cprovider = generator.getClipboardProvider();
             Clipboard clip=null;
             GridRandom random = grid.getRandom();
             List<Clipboard> clips;
             if(hasNorth){
                 if(hasSouth){
                     if(hasEast){
                         if(hasWest){ // X
                             clips = getFits(cprovider,RoadType.STREET_X);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_x-cross_v1.schematic");
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }else { // N S E
                             clips = getFits(cprovider,RoadType.STREET_T_E);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_t-cross-SN_v1.schematic"); // maybe  SN
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }
                     }else {
                         if(hasWest){ // N S W
                             clips = getFits(cprovider,RoadType.STREET_T_W);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_t-cross-NS_v1.schematic"); // maybe  NS
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }else { // N S
                             clips = getFits(cprovider,RoadType.STREET_I_NS);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_straight-NS_v1.schematic");
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }
                     }
 
                 }else {
                     if(hasEast){
                         if(hasWest){ // N E W
                             clips = getFits(cprovider,RoadType.STREET_T_N);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_t-cross-EW_v1.schematic"); // maybe  WE
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }else { // N E
                             clips = getFits(cprovider,RoadType.STREET_C_NE);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_curve-EN_v1.schematic");
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }
                     }else {
                         if(hasWest){ // N W
                             clips = getFits(cprovider,RoadType.STREET_C_NW);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_curve-WN_v1.schematic");
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }else { // N
                             clip = generator.getClipboardProvider().getByName("street_deadend-N_v1.schematic"); //TODO no deadends!
                         }
                     }
                 }
             }else {
                 if(hasSouth){
                     if(hasEast){
                         if(hasWest){ // E W S
                             clips = getFits(cprovider,RoadType.STREET_T_S);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_t-cross-WE_v1.schematic"); // maybe  EW
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }else { // S E
                             clips = getFits(cprovider,RoadType.STREET_C_SE);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_curve_SE_v1.schematic");
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }
                     }else {
                         if(hasWest){ // S W
                             clips = getFits(cprovider,RoadType.STREET_C_SW);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_curve_SW_v1.schematic");
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }else { // S
                             clip = generator.getClipboardProvider().getByName("street_deadend-S_v1.schematic");
                         }
                     }
 
                 }else {
                     if(hasEast){
                         if(hasWest){ // E W
                             clips = getFits(cprovider,RoadType.STREET_I_EW);
                             if(clips==null||clips.size()==0){
                                 generator.reportDebug("Can't find road fit, using hardcoded fallback.");
                                 clip = generator.getClipboardProvider().getByName("street_straight-WE_v1.schematic");
                             }else {
                                 clip = clips.get(random.getRandomInt(clips.size()));
                             }
                         }else { // E
                             clip = generator.getClipboardProvider().getByName("street_deadend-E_v1.schematic");
                         }
                     }else {
                         if(hasWest){ // W
                             clip = generator.getClipboardProvider().getByName("street_deadend_W_v1.schematic");
                         }else { // none, is an isolated road
                             generator.reportDebug("found an isolated roadcell, nothing placed"); // better solution?
                         }
                     }
                 }
             }
             if(clip!=null){
                 clip.paste(generator,chunkX<<4,chunkZ<<4, Constants.BUILD_HEIGHT); // FIXME Hardcoded street level
                 decayRoadChunk(generator,chunk,Constants.BUILD_HEIGHT-2);
                 decaySidewalk(generator,chunk,Constants.BUILD_HEIGHT-1);
             }
 
         }else{
             generator.reportDebug("Wanted to place road where it should not belong...");
         }
     }
 
     private List<Clipboard> getFits(ClipboardProvider cprovider,RoadType type){
         return cprovider.getRoadFit(1, 1, Direction.NONE, ContextType.STREET, type);
     }
 
     @Override
     public String toString() {
        return "RoadParcel +[" + chunkX +"]["+chunkZ+"]";
     }
 }
 
