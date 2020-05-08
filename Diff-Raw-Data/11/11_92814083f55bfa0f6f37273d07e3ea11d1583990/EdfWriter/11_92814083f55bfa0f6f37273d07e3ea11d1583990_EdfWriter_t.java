 package com.github.dreamrec.edf;
 
 import com.github.dreamrec.AdsDataListener;
 import com.github.dreamrec.HiPassPreFilter;
 import com.github.dreamrec.ads.AdsModel;
 import com.github.dreamrec.ads.ChannelModel;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  */
 public class EdfWriter implements AdsDataListener {
     private static final int RECORD_PERIOD = 1;  // duration of EDF data record (in seconds)
 
     private static final Log log = LogFactory.getLog(EdfWriter.class);
     private RandomAccessFile outStream = null;
     private AdsModel adsModel;
     private int[] edfFrame;
     private int inputFramesCounter;
     private int inputFramesPerRecord;
     private long startTime;
     private int numberOfDataRecords = -1;
 
 
     public EdfWriter(AdsModel adsModel) {
         this.adsModel = adsModel;
         openFile();
         inputFramesPerRecord = (adsModel.getSps().getValue() / AdsModel.MAX_DIV) * RECORD_PERIOD;
         edfFrame = new int[inputFramesPerRecord * adsModel.getFrameSize()];
         startTime = System.currentTimeMillis();
         try {
             outStream.writeChars(createEdfHeader());
         } catch (IOException e) {
             log.error(e);
         }
     }
 
     public void stopRecording() {
         try {
             outStream.seek(0);
             outStream.writeChars(createEdfHeader());
             outStream.close();
         } catch (IOException e) {
             log.error(e);
         }
     }
 
     @Override
     public void onDataReceived(int[] dataFrame) {
         ArrayList<ChannelModel> activeChannels = adsModel.getActiveChannels();
         int channelPosition = 0;
         for (ChannelModel channel : activeChannels) {
             int channelSampleNumber = AdsModel.MAX_DIV / channel.getDivider();
             HiPassPreFilter channelFilter = channel.getHiPassPreFilter();
             for (int j = 0; j < channelSampleNumber; j++) {
                 int filteredValue = channelFilter.getFilteredValue(dataFrame[channelPosition + j]);
                 edfFrame[channelPosition * inputFramesPerRecord + inputFramesCounter * channelSampleNumber + j] = filteredValue;
             }
             channelPosition += channelSampleNumber;
         }
         inputFramesCounter++;
         if (inputFramesCounter == inputFramesPerRecord) {  // when edfFrame is ready
             // change format to Little_endian and save to file
             for (int i = 0; i < edfFrame.length; i++) {
                 Short element = (short) edfFrame[i];
                 try {
                     outStream.writeShort(toLittleEndian(element));
                 } catch (IOException e) {
                     log.error(e);
                 }
             }
             inputFramesCounter = 0;
            if (numberOfDataRecords == -1) {
                numberOfDataRecords = 1;
            } else {
                numberOfDataRecords++;
            }
         }
     }
 
     private void openFile() {
         Date date = new Date(System.currentTimeMillis());
         String fileName = new SimpleDateFormat("ss_mm_HH_dd_MM_yyyy").format(date) + ".edf";
         try {
             outStream = new RandomAccessFile(fileName, "rw");
         } catch (Exception e) {
             log.error(e);
             //throw new ApplicationException("Error while creating file " + fileName);
         }
     }
 
     /*
     HEADER RECORD
     8 ascii : version of this data format (0)
     80 ascii : local patient identification
     80 ascii : local recording identification
     8 ascii : startdate of recording (dd.mm.yy)
     8 ascii : starttime of recording (hh.mm.ss)
     8 ascii : number of bytes in header record
     44 ascii : reserved
     8 ascii : number of data records (-1 if unknown)
     8 ascii : duration of a data record, in seconds
     4 ascii : number of signals (ns) in data record = number of active channels
     ns * 16 ascii : ns * label (e.g. EEG Fpz-Cz or Body temp)
     ns * 80 ascii : ns * transducer type (e.g. AgAgCl electrode)
     ns * 8 ascii : ns * physical dimension (e.g. uV or degreeC)
     ns * 8 ascii : ns * physical minimum (e.g. -500 or 34)
     ns * 8 ascii : ns * physical maximum (e.g. 500 or 40)
     ns * 8 ascii : ns * digital minimum (e.g. -2048)
     ns * 8 ascii : ns * digital maximum (e.g. 2047)
     ns * 80 ascii : ns * prefiltering (e.g. HP:0.1Hz LP:75Hz)
     ns * 8 ascii : ns * nr of samples in each data record
     ns * 32 ascii : ns * reserved
 
     ns - number of signals
     */
     public String createEdfHeader() {
         StringBuilder edfHeader = new StringBuilder();
 
         String version = "0";
         String localPatientIdentification = "Rabbit";
         String localRecordingIdentification = "Tralivali";
 
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
         SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm.ss");
 
         String startDateOfRecording = dateFormat.format(new Date(startTime));
         String startTimeOfRecording = timeFormat.format(new Date(startTime));
 
         int numberOfSignals = adsModel.getNumberOfActiveChannels();  // number of signals in data record = number of active channels
         int numberOfBytesInHeaderRecord = (8 * 6 + 80 * 2 + 44 + 4) + numberOfSignals * (16 + 80 * 2 + 32 + 8 * 6);
         String reserved = "";
 
         String durationOfDataRecord = Integer.toString(RECORD_PERIOD);
 
         String channelsPreFiltering = "HP:0.05Hz LP:250Hz N:50Hz";  // todo function(channel.hiPassBufferSize)
         String channelsTransducerType = "AgAgCl cup electrodes";
         String channelsDigitalMaximum = "32767";
         String channelsDigitalMinimum = "-32767";
         String channelsPhysicalMaximum = "6304";  // todo function(channel.gain)
         String channelsPhysicalMinimum = "-6304"; // todo function(channel.gain)
 
         String accelerometerPreFiltering = "HP:0.05Hz LP:250Hz N:50Hz"; // todo function(channel.hiPassBufferSize)
         String accelerometerTransducerType = "none";
         String accelerometerDigitalMaximum = "1024";
         String accelerometerDigitalMinimum = "-1024";
         String accelerometerPhysicalMaximum = "10";
         String accelerometerPhysicalMinimum = "0";
 
         edfHeader.append(adjustLength(version, 8));
         edfHeader.append(adjustLength(localPatientIdentification, 80));
         edfHeader.append(adjustLength(localRecordingIdentification, 80));
         edfHeader.append(startDateOfRecording);
         edfHeader.append(startTimeOfRecording);
         edfHeader.append(adjustLength(Integer.toString(numberOfBytesInHeaderRecord), 8));
         edfHeader.append(adjustLength(reserved, 44));
         edfHeader.append(adjustLength(Integer.toString(numberOfDataRecords), 8));
         edfHeader.append(adjustLength(durationOfDataRecord, 8));
         edfHeader.append(adjustLength(Integer.toString(numberOfSignals), 4));
 
         StringBuilder labels = new StringBuilder();
         StringBuilder transducerTypes = new StringBuilder();
         StringBuilder physicalDimensions = new StringBuilder();
         StringBuilder physicalMinimums = new StringBuilder();
         StringBuilder physicalMaximums = new StringBuilder();
         StringBuilder digitalMinimums = new StringBuilder();
         StringBuilder digitalMaximums = new StringBuilder();
         StringBuilder preFilterings = new StringBuilder();
         StringBuilder samplesNumbers = new StringBuilder();
         StringBuilder reservedForChannels = new StringBuilder();
 
 
         for (ChannelModel channel : adsModel.getAdsActiveChannels()) {
             labels.append(adjustLength(channel.getName(), 16));
             transducerTypes.append(adjustLength(channelsTransducerType, 80));
             physicalDimensions.append(adjustLength(channel.PHYSICAL_DIMENSION, 8));
             physicalMinimums.append(adjustLength(channelsPhysicalMinimum, 8));
             physicalMaximums.append(adjustLength(channelsPhysicalMaximum, 8));
             digitalMinimums.append(adjustLength(channelsDigitalMinimum, 8));
             digitalMaximums.append(adjustLength(channelsDigitalMaximum, 8));
 
             preFilterings.append(adjustLength(channelsPreFiltering, 80));
 
             int nrOfSamplesInEachDataRecord = RECORD_PERIOD * adsModel.getSps().getValue() / channel.getDivider();
 
             samplesNumbers.append(adjustLength(Integer.toString(nrOfSamplesInEachDataRecord), 8));
             reservedForChannels.append(adjustLength(reserved, 32));
         }
         for (ChannelModel channel : adsModel.getAccelerometerActiveChannels()) {
             labels.append(adjustLength(channel.getName(), 16));
             transducerTypes.append(adjustLength(accelerometerTransducerType, 80));
             physicalDimensions.append(adjustLength(channel.PHYSICAL_DIMENSION, 8));
             physicalMinimums.append(adjustLength(accelerometerPhysicalMinimum, 8));
             physicalMaximums.append(adjustLength(accelerometerPhysicalMaximum, 8));
             digitalMinimums.append(adjustLength(accelerometerDigitalMinimum, 8));
             digitalMaximums.append(adjustLength(accelerometerDigitalMaximum, 8));
 
             preFilterings.append(adjustLength(accelerometerPreFiltering, 80));
 
             int nrOfSamplesInEachDataRecord = RECORD_PERIOD * adsModel.getSps().getValue() / channel.getDivider();
 
             samplesNumbers.append(adjustLength(Integer.toString(nrOfSamplesInEachDataRecord), 8));
             reservedForChannels.append(adjustLength(reserved, 32));
         }
 
         edfHeader.append(labels);
         edfHeader.append(transducerTypes);
         edfHeader.append(physicalDimensions);
         edfHeader.append(physicalMinimums);
         edfHeader.append(physicalMaximums);
         edfHeader.append(digitalMinimums);
         edfHeader.append(digitalMaximums);
         edfHeader.append(preFilterings);
         edfHeader.append(samplesNumbers);
         edfHeader.append(reservedForChannels);
 
         return edfHeader.toString();
     }
 
 
     /**
      * if the String.length() is more then the given length we cut the String
      * if the String.length() is less then the given length we append spaces to the end of the String
      */
 
     private String adjustLength(String text, int length) {
         StringBuilder sB = new StringBuilder(text);
         if (text.length() > length) {
             sB.delete((length + 1), text.length());
 
         } else {
             for (int i = text.length(); i < length; i++) {
                 sB.append(" ");
             }
         }
         return sB.toString();
     }
 
     /**
      * change Big_endian format of numbers (java)  to Little_endian format (for edf and microcontroller)
      */
     private Short toLittleEndian(Short value) {
         int capacity = 2;
         return ByteBuffer.allocate(capacity)
                 .order(ByteOrder.BIG_ENDIAN).putShort(value)
                 .order(ByteOrder.LITTLE_ENDIAN).getShort(0);
     }
 }
