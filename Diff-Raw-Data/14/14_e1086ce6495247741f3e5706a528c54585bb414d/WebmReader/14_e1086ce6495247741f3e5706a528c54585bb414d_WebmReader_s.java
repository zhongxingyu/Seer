 package com.livejournal.karino2.whiteboardcast;
 
 import com.google.libwebm.mkvparser.Block;
 import com.google.libwebm.mkvparser.BlockEntry;
 import com.google.libwebm.mkvparser.Cluster;
 import com.google.libwebm.mkvparser.EbmlHeader;
 import com.google.libwebm.mkvparser.Frame;
 import com.google.libwebm.mkvparser.MkvReader;
 import com.google.libwebm.mkvparser.Track;
 import com.google.libwebm.mkvparser.Tracks;
 
 /**
  * Created by karino on 6/28/13.
  */
 public class WebmReader {
     StringBuilder error = new StringBuilder();
     MkvReader reader;
     com.google.libwebm.mkvparser.Segment parserSegment;
 
     public boolean open(String path) {
         reader = new MkvReader();
         if(0 != reader.open(path)) {
             error.append("Input file is invalid or error while opening.");
             return false;
         }
         EbmlHeader ebmlHeader = new EbmlHeader();
         long[] outputPosition = {0};
         ebmlHeader.parse(reader, outputPosition);
         long position = outputPosition[0];
         com.google.libwebm.mkvparser.Segment[] outputParserSegment = {null};
         long result = com.google.libwebm.mkvparser.Segment.createInstance(
                 reader, position, outputParserSegment);
         parserSegment = outputParserSegment[0];
         if (result != 0) {
             error.append("Segment.createInstance() failed.");
             return false;
         }
 
         result = parserSegment.load();
         if (result < 0) {
            error.append("Segment.load() failed.");
             return false;
         }
 
         com.google.libwebm.mkvparser.SegmentInfo parserSegmentInfo = parserSegment.getInfo();
         long timeCodeScale = parserSegmentInfo.getTimeCodeScale();
 
 
 
         // VideoTrack firstTrack = (VideoTrack)parserTracks.getTrackByIndex(0);
         currentTrackNumber = 0;
 
         return true;
     }
 
     public void initTracks() {
         currentTrackNumber = 0;
         parserTracks = parserSegment.getTracks();
         updateCurrentTrack();
     }
 
     private void updateCurrentTrack() {
         Track track = parserTracks.getTrackByIndex(currentTrackNumber);
         currentTrack = track;
     }
 
     Tracks parserTracks;
     Track currentTrack;
 
     public long getTracksCount() {
         return parserTracks.getTracksCount();
     }
 
     public Track getCurrentTrack() {
         return currentTrack;
     }
 
 
     Cluster cluster;
     public boolean initCluster() {
         cluster = parserSegment.getFirst();
         return cluster != null;
     }
 
     public Cluster getCurrentCluster() {
         return cluster;
     }
 
     public boolean gotoNextCluster() {
         cluster = parserSegment.getNext(cluster);
         return cluster != null;
     }
 
 
     public boolean initBlockEntry() {
         BlockEntry[] outputBlockEntry = {null};
         long status = cluster.getFirst(outputBlockEntry);
         blockEntry = outputBlockEntry[0];
         if (status != 0) {
             return false;
         }
         return true;
     }
     BlockEntry blockEntry;
     public BlockEntry getFirstBlockEntry() {
         if(initBlockEntry())
             return blockEntry;
         return null;
     }
 
     public boolean gotoNextBlockEntry() {
         BlockEntry[] outputNext = {null};
         long status = cluster.getNext(blockEntry, outputNext);
         blockEntry = outputNext[0];
         if (status != 0) {
             return false;
         }
         if(blockEntry == null || blockEntry.eos())
             return false;
         setupNewBlockEntry(blockEntry);
         return true;
 
     }
     public BlockEntry getCurrentBlockEntry() {
         return blockEntry;
     }
 
     Block block;
     public boolean initFrameQueue() {
         BlockEntry be = getFirstBlockEntry();
         if(be == null || be.eos())
             return false;
         setupNewBlockEntry(be);
         return true;
     }
 
     private void setupNewBlockEntry(BlockEntry be) {
         block = be.getBlock();
         frameCount = block.getFrameCount();
         currentFrame = 0;
     }
     public boolean isKey() { return block.isKey(); }
 
     public long getBlockTimeNS() {
         return block.getTime(cluster);
     }
 
     boolean done = false;
     public boolean isDone() {
         return done;
     }
 
     int frameCount;
     int currentFrame;
     public byte[] popFrame() {
         if(currentFrame == frameCount) {
             if(!gotoNextBlockEntry()){
                 if(!gotoNextCluster()) {
                     done = true;
                     return null;
                 }
                 if(!initFrameQueue()) {
                     done = true;
                     return null;
                 }
                 // fall through
             }
         }
         int cur = currentFrame++;
         Frame frame = block.getFrame(cur);
         byte[] data;
         byte[][] outputData = {null};
         long result = frame.read(reader, outputData);
         if(result != 0)
             return null;
         data = outputData[0];
         return data;
     }
 
 
     // currently, only one track.
     /*
     public boolean gotoNextTrack() {
         if(getTracksCount() == currentTrackNumber)
             return false;
         currentTrackNumber++;
         if(getTracksCount() == currentTrackNumber)
             return false;
         updateCurrentTrack();
         return true;
     }
     */
 
     long currentTrackNumber;
 
     public void close() {
         reader.close();
     }
 }
