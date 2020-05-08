 package dk.statsbiblioteket.broadcasttranscoder.cli.parsers;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.OptionParseException;
 import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
 import dk.statsbiblioteket.broadcasttranscoder.cli.UsageException;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.TranscodingRecord;
 import org.apache.commons.cli.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import static dk.statsbiblioteket.broadcasttranscoder.cli.PropertyNames.*;
 
 /**
  *
  */
 public class SingleTranscodingOptionsParser<T extends TranscodingRecord> extends InfrastructureOptionsParser<T> {
 
     protected static final Option PID_OPTION = new Option("programpid", true, "The DOMS pid of the program to be transcoded");
     protected static final Option TIMESTAMP_OPTION = new Option("timestamp", true, "The timestamp (milliseconds) for which transcoding is required");
     protected static final Option BEHAVIOURAL_CONFIG_FILE_OPTION = new Option("behavioural_configfile", true, "The behavioural config file");
 
 
 
     private SingleTranscodingContext<T> context;
 
     public SingleTranscodingOptionsParser() {
         super();
         context = new SingleTranscodingContext<T>();
         getOptions().addOption(PID_OPTION);
         getOptions().addOption(TIMESTAMP_OPTION);
         getOptions().addOption(BEHAVIOURAL_CONFIG_FILE_OPTION);
     }
 
 
     public SingleTranscodingContext<T> parseOptions(String[] args) throws OptionParseException, UsageException {
         CommandLineParser parser = new PosixParser();
         CommandLine cmd;
         try {
             cmd = parser.parse(getOptions(), args);
         } catch (ParseException e) {
             parseError(e.toString());
             throw new OptionParseException(e.getMessage(), e);
         }
         parseUsageOption(cmd);
         parseInfrastructureConfigFileOption(cmd);
         parseBehaviouralConfigFileOption(cmd);
         parseHibernateConfigFileOption(cmd);
         parseProgramPid(cmd);
         parseTimestampOption(cmd);
         try {
             readInfrastructureProperties(context);
             readBehaviouralProperties(context);
         } catch (IOException e) {
             throw new OptionParseException("Error reading properties.", e);
         }
         return context;
     }
 
 
 
 
     protected static void readBehaviouralProperties(SingleTranscodingContext context) throws IOException, OptionParseException {
         Properties props = new Properties();
         props.load(new FileInputStream(context.getBehaviourConfigFile()));
         context.setVideoBitrate(readIntegerProperty(VIDEO_BITRATE, props));
         context.setAudioBitrate(readIntegerProperty(AUDIO_BITRATE, props));
         context.setVideoHeight(readIntegerProperty(HEIGHT, props));
         context.setVlcTranscodingString(readStringProperty(VLC_TRANSCODING_STRING, props));
         context.setFfmpegTranscodingString(readStringProperty(FFMPEG_TRANSCODING_STRING, props));
         context.setTranscodingTimeoutDivisor(readFloatProperty(TRANSCODING_DIVISOR, props));
         context.setAnalysisClipLength(readLongProperty(ANALYSIS_CLIP_LENGTH, props));
         context.setStartOffsetTS(readIntegerProperty(START_OFFSET_TS, props));
         context.setEndOffsetTS(readIntegerProperty(END_OFFSET_TS, props));
         context.setStartOffsetTSWithTVMeter(readIntegerProperty(START_OFFSET_TS_WITH_TVMETER, props));
         context.setEndOffsetTSWithTVMeter(readIntegerProperty(END_OFFSET_TS_WITH_TVMETER, props));
         context.setStartOffsetPS(readIntegerProperty(START_OFFSET_PS, props));
         context.setEndOffsetPS(readIntegerProperty(END_OFFSET_PS, props));
         context.setStartOffsetPSWithTVMeter(readIntegerProperty(START_OFFSET_PS_WITH_TVMETER, props));
         context.setEndOffsetPSWithTVMeter(readIntegerProperty(END_OFFSET_PS_WITH_TVMETER, props));
         context.setStartOffsetWAV(readIntegerProperty(START_OFFSET_WAV, props));
         context.setEndOffsetWAV(readIntegerProperty(END_OFFSET_WAV, props));
         context.setMaxMissingStart(readIntegerProperty(MAX_MISSING_START, props));
         context.setMaxMissingEnd(readIntegerProperty(MAX_MISSING_END, props));
         context.setMaxHole(readIntegerProperty(MAX_HOLE_SIZE, props));
         context.setGapToleranceSeconds(readIntegerProperty(GAP_TOLERANCE, props));
         context.setPreviewLength(readIntegerProperty(PREVIEW_LENGTH, props));
         context.setPreviewTimeout(readIntegerProperty(PREVIEW_TIMEOUT, props));
         context.setSnapshotFrames(readIntegerProperty(SNAPSHOT_FRAMES, props));
         context.setSnapshotPaddingSeconds(readIntegerProperty(SNAPSHOT_PADDING, props));
         context.setSnapshotScale(readIntegerProperty(SNAPSHOT_SCALE, props));
         context.setSnapshotTargetDenominator(readIntegerProperty(SNAPSHOT_TARGET_DENOMINATIOR, props));
         context.setSnapshotTargetNumerator(readIntegerProperty(SNAPSHOT_TARGET_NUMERATOR, props));
         context.setSnapshotTimeoutDivisor(readFloatProperty(SNAPSHOT_TIMEOUT_DIVISOR, props));
         context.setSoxTranscodeParams(readStringProperty(SOX_TRANSCODE_PARAMS, props));
         context.setDefaultTranscodingTimestamp(readLongProperty(DEFAULT_TIMESTAMP, props));
         context.setOverwrite(readBooleanProperty(OVERWRITE,props));
         context.setOnlyTranscodeChanges(readBooleanProperty(ONLYTRANSCODECHANGES, props));
         context.setVideoOutputSuffix(readStringProperty(VIDEO_OUTPUT_SUFFIX, props));
         context.setVlcRemuxingString(readStringProperty(VLC_REMUXING_STRING, props));
        context.setDomsViewAngle(readStringProperty(DOMS_VIEWANGLE, props));
     }
 
 
 
 
     protected void parseBehaviouralConfigFileOption(CommandLine cmd) throws OptionParseException {
             String configFileString = cmd.getOptionValue(BEHAVIOURAL_CONFIG_FILE_OPTION.getOpt());
             if (configFileString == null) {
                 parseError(BEHAVIOURAL_CONFIG_FILE_OPTION.toString());
                 throw new OptionParseException(BEHAVIOURAL_CONFIG_FILE_OPTION.toString());
             }
             File configFile = new File(configFileString);
             if (!configFile.exists() || configFile.isDirectory()) {
                 throw new OptionParseException(configFile.getAbsolutePath() + " is not a file.");
             }
             context.setBehaviourConfigFile(configFile);
         }
 
 
     @Override
     protected SingleTranscodingContext<T> getContext() {
         return context;
     }
 
 
     protected void parseProgramPid(CommandLine cmd) throws OptionParseException {
         String programPid = cmd.getOptionValue(PID_OPTION.getOpt());
         if (programPid == null) {
             parseError(PID_OPTION.toString());
             throw new OptionParseException(PID_OPTION.toString());
         } else {
             context.setProgrampid(programPid);
         }
     }
 
 
     protected void parseTimestampOption(CommandLine cmd) throws OptionParseException {
         String timestampString = cmd.getOptionValue(TIMESTAMP_OPTION.getOpt());
         if (timestampString == null) {
             parseError(TIMESTAMP_OPTION.toString());
             throw new OptionParseException(TIMESTAMP_OPTION.toString());
         } else {
             context.setTranscodingTimestamp(Long.parseLong(timestampString));
         }
     }
 
 
 }
