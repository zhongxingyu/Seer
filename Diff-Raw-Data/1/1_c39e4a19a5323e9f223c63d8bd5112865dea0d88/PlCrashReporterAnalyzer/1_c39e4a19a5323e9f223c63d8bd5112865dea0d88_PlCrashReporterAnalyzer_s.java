 package com.wyntersoft.crashreporteranalyzer;
 
 import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
 import com.google.common.base.Function;
 import com.google.common.collect.Ordering;
 import com.wyntersoft.crashreporteranalyzer.*;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import coop.plausible.crashreporter.CrashReport_pb;
 import coop.plausible.crashreporter.CrashReport_pb.CrashReport.BinaryImage;
 import coop.plausible.crashreporter.CrashReport_pb.CrashReport.Processor.TypeEncoding;
 import coop.plausible.crashreporter.CrashReport_pb.CrashReport.Thread;
 import coop.plausible.crashreporter.CrashReport_pb.CrashReport.Thread.StackFrame;
 import coop.plausible.crashreporter.CrashReport_pb.CrashReport.Processor;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.CharBuffer;
 import java.nio.channels.FileChannel;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.lang.Math;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dwarren
  * Date: 11/25/12
  * Time: 8:40 AM
  * To change this template use File | Settings | File Templates.
  */
 public class PlCrashReporterAnalyzer {
     private final String unknownString = "???";
     static final int NUM_FINGERPRINT_FRAMES = 7;
     static final int CPU_ARCH_ABI64	= 0x01000000;		/* 64 bit ABI */
 
     public enum CpuType {
         CPU_TYPE_ANY        (-1),
         CPU_TYPE_VAX        (1),
         CPU_TYPE_MC680x0	(6),
         CPU_TYPE_X86		(7),
         CPU_TYPE_X86_64     (CPU_TYPE_X86.getValue()|CPU_ARCH_ABI64),
         CPU_TYPE_MC98000	(10),
         CPU_TYPE_HPPA       (11),
         CPU_TYPE_ARM		(12),
         CPU_TYPE_MC88000	(13),
         CPU_TYPE_SPARC		(14),
         CPU_TYPE_I860		(15),
         CPU_TYPE_POWERPC	(18),
         CPU_TYPE_POWERPC64  (CPU_TYPE_POWERPC.getValue()|CPU_ARCH_ABI64);
 
         public static CpuType valueOf(int value) {
             switch (value) {
                 case -1: return CPU_TYPE_ANY;
                 case 1: return CPU_TYPE_VAX;
                 case 6: return CPU_TYPE_MC680x0;
                 case 7: return CPU_TYPE_X86;
                 case 7|CPU_ARCH_ABI64: return CPU_TYPE_X86_64;
                 case 10: return CPU_TYPE_MC98000;
                 case 11: return CPU_TYPE_HPPA;
                 case 12: return CPU_TYPE_ARM;
                 case 13: return CPU_TYPE_MC88000;
                 case 14: return CPU_TYPE_SPARC;
                 case 15: return CPU_TYPE_I860;
                 case 18: return CPU_TYPE_POWERPC;
                 case 18|CPU_ARCH_ABI64: return CPU_TYPE_POWERPC64;
 
                 default: return null;
             }
         }
 
         private int code;
         CpuType(int c) { code = c;}
         public int getValue() { return code; }
 
         boolean isLp64() { return (this.getValue() & CPU_ARCH_ABI64) !=0;}
 
     }
 
     public enum CpuSubTypeArm {
         CPU_SUBTYPE_ARM_ALL     (0),
         CPU_SUBTYPE_ARM_V4T     (5),
         CPU_SUBTYPE_ARM_V6      (6),
         CPU_SUBTYPE_ARM_V5TEJ   (7),
         CPU_SUBTYPE_ARM_XSCALE	(8),
         CPU_SUBTYPE_ARM_V7		(9),
         CPU_SUBTYPE_ARM_V7F		(10), /* Cortex A9 */
         CPU_SUBTYPE_ARM_V7S     (11),
         CPU_SUBTYPE_ARM_V7K		(12); /* Kirkwood40 */
 
         public static CpuSubTypeArm valueOf(int value) {
             switch (value) {
                 case 0: return CPU_SUBTYPE_ARM_ALL;
                 case 5: return CPU_SUBTYPE_ARM_V4T;
                 case 6: return CPU_SUBTYPE_ARM_V6;
                 case 7: return CPU_SUBTYPE_ARM_V5TEJ;
                 case 8: return CPU_SUBTYPE_ARM_XSCALE;
                 case 9: return CPU_SUBTYPE_ARM_V7;
                 case 10:return CPU_SUBTYPE_ARM_V7F;
                 case 11:return CPU_SUBTYPE_ARM_V7S;
                 case 12:return CPU_SUBTYPE_ARM_V7K;
 
                 default: return null;
             }
         }
         private int code;
         CpuSubTypeArm(int c) { code = c;}
         public int getValue() { return code; }
 
     }
 
     public PlCrashReporterAnalyzer(ByteBuffer buffer) throws InvalidProtocolBufferException, IOException {
         InitFromByteBuffer(buffer);
     }
 
     public PlCrashReporterAnalyzer(byte[] buffer) throws Exception
     {
         InitFromByteBuffer(ByteBuffer.wrap(buffer));
     }
 
     public PlCrashReporterAnalyzer(String path) throws Exception {
         FileChannel inChannel = new RandomAccessFile(path, "r").getChannel();
 
         if (inChannel.size() > Integer.MAX_VALUE) {
             throw new IOException("Dump file too large");
         }
 
         ByteBuffer buffer = ByteBuffer.allocate((int)inChannel.size());
         int nBytesRead = inChannel.read(buffer);
 
         InitFromByteBuffer(buffer);
     }
 
     public String getOperatingSystem() {
         if (!report.hasSystemInfo()) {
             return "Not Reported";
         }
 
         switch (report.getSystemInfo().getOperatingSystem()) {
             case MAC_OS_X:
                 return "Mac OS X";
             case IPHONE_OS:
                 return "iOS";
             case IPHONE_SIMULATOR:
                 return "iOS Simulator";
             case OS_UNKNOWN:
                 return "Unknown";
         }
 
         return null;
     }
 
     public CpuType getCpuType() {
         for(BinaryImage image : report.getBinaryImagesList()) {
             if (!image.hasCodeType())
                 continue;
 
             if (image.getCodeType().getEncoding() != TypeEncoding.TYPE_ENCODING_MACH)
                 continue;
 
             if (CpuType.valueOf((int)image.getCodeType().getType()) != null)
                 return CpuType.valueOf((int)image.getCodeType().getType());
         }
 
         switch (report.getSystemInfo().getArchitecture()) {
             case ARMV6:
             case ARMV7:
                 return CpuType.CPU_TYPE_ARM;
             case X86_32:
                 return CpuType.CPU_TYPE_X86;
             case X86_64:
                 return CpuType.CPU_TYPE_X86_64;
             case PPC:
                 return CpuType.CPU_TYPE_POWERPC;
         }
 
         return CpuType.CPU_TYPE_ANY;
     }
 
     public String getCodeType() {
         switch (getCpuType()) {
             case CPU_TYPE_ARM:
                 return "ARM";
             case CPU_TYPE_X86:
                 return "X86";
             case CPU_TYPE_X86_64:
                 return "X86-64";
             case CPU_TYPE_POWERPC:
                 return "PPC";
         }
         return String.format("Unknown (%d)", (int)report.getSystemInfo().getArchitecture().getNumber());
     }
 
     public String getHardwareModel() {
         if (report.hasMachineInfo() && report.getMachineInfo().getModel() != null)
             return report.getMachineInfo().getModel();
 
         return unknownString;
     }
 
     public String getCrashReport() {
         StringBuilder sb = new StringBuilder();
         // Preamble
         sb.append("Incident Identifier:   [TODO]\n")
           .append("CrashReporter Key:     [TODO]\n");
 
         // Machine info
         sb.append(String.format("Hardware Model:        %s\n",getHardwareModel()));
 
         // Process Info
         String processName = unknownString;
         String processId = unknownString;
         String processPath = unknownString;
         String parentProcessName = unknownString;
         String parentProcessId = unknownString;
         if (report.hasProcessInfo()) {
             if (report.getProcessInfo().getProcessName() != null)
                 processName = report.getProcessInfo().getProcessName();
 
             processId = String.format("%d", report.getProcessInfo().getProcessId());
 
             if (report.getProcessInfo().hasProcessPath())
                 processPath = report.getProcessInfo().getProcessPath();
 
             if (report.getProcessInfo().getParentProcessName() != null)
                 parentProcessName = report.getProcessInfo().getParentProcessName();
 
             parentProcessId = String.format("%d", report.getProcessInfo().getParentProcessId());
         }
 
         sb.append(String.format("Process:               %s [%s]\n", processName, processId))
           .append(String.format("Path:                  %s\n", processPath))
           .append(String.format("Identifier:            %s\n", report.getApplicationInfo().getIdentifier()))
           .append(String.format("Version:               %s\n", report.getApplicationInfo().getVersion()))
           .append(String.format("Code Type:             %s\n", getCodeType()))
           .append(String.format("Parent Process         %s [%s]\n", parentProcessName, parentProcessId))
           .append("\n");
 
         // System info
         String osBuild = unknownString;
         if (report.getSystemInfo().hasOsBuild())
             osBuild = report.getSystemInfo().getOsBuild();
 
         sb.append(String.format("Date/Time:             %s\n", new Date(report.getSystemInfo().getTimestamp() * 1000)))
           .append(String.format("OS Version:            %s %s (%s)\n", getOperatingSystem(), report.getSystemInfo().getOsVersion(), osBuild))
           .append("Report Version:        104\n")
           .append("\n");
 
         sb.append(getSignalString());
 
         Thread crashedThread = getCrashedThread();
         if (crashedThread != null) {
                 sb.append(String.format("Crashed Thread:        %s\n", crashedThread.getThreadNumber()));
         }
         sb.append("\n");
 
         // Uncaught Exceptions
         sb.append(getExceptionString());
 
         // Threads
         int maxThreadNum = 0;
 
         for (Thread thread : report.getThreadsList()) {
             if (thread.getCrashed()) {
                 sb.append(String.format("Thread %d Crashed:\n", thread.getThreadNumber()));
             } else {
                 sb.append(String.format("Thread %d:\n", thread.getThreadNumber()));
             }
 
             long frameIdx = 0;
             for(StackFrame frame : thread.getFramesList()) {
                 sb.append(getStackFrameInfo(frame, frameIdx));
                 frameIdx++;
             }
 
             maxThreadNum = Math.max(maxThreadNum, thread.getThreadNumber());
 
             sb.append("\n");
         }
 
         // Registers
         if (crashedThread != null) {
             sb.append(String.format("Thread %d crashed with %s Thread State:\n", crashedThread.getThreadNumber(), getCodeType()));
 
             boolean lp64 = getCpuType().isLp64();
             int regColumn = 0;
             for(Thread.RegisterValue register : crashedThread.getRegistersList()) {
                 String reg_fmt;
 
                 /* Use 32-bit or 64-bit fixed width format for the register values */
                 if (lp64) {
                     reg_fmt = "%6s: 0x%016x ";
                 } else {
                     reg_fmt = "%6s: 0x%08x ";
                 }
                 /* Remap register names to match Apple's crash reports */
                 String regName = register.getName();
 
                 if (report.hasMachineInfo() &&
                     report.getMachineInfo().getProcessor().getEncoding() == TypeEncoding.TYPE_ENCODING_MACH) {
 
                     Processor processor = report.getMachineInfo().getProcessor();
                     CpuType type = CpuType.valueOf((int)processor.getType());
 
                     /* Apple uses 'ip' rather than 'r12' on ARM */
                     if (type == CpuType.CPU_TYPE_ARM && regName.equals("r12")) {
                         regName = "ip";
                     }
                 }
 
                 sb.append(String.format(reg_fmt, regName, register.getValue()));
 
                 regColumn++;
                 if (regColumn == 4) {
                     sb.append("\n");
                     regColumn = 0;
                 }
             }
 
             if (regColumn != 0) {
                 sb.append("\n");
             }
 
             sb.append("\n");
         }
 
         /* Images. The iPhone crash report format sorts these in ascending order, by the base address */
         sb.append("Binary Images:\n");
 
         List<BinaryImage> imageList = Ordering.natural().onResultOf(new Function<BinaryImage, Long>() {
             public Long apply(BinaryImage foo) {
                 return new Long(foo.getBaseAddress());
             }
         }).sortedCopy(report.getBinaryImagesList());
         for (BinaryImage image : imageList) {
             String uuid = unknownString;
             if (image.hasUuid()) {
                 ByteBuffer uuidBytes = ByteBuffer.allocate(16);
                 image.getUuid().copyTo(uuidBytes);
                 uuidBytes.order(ByteOrder.BIG_ENDIAN);
                 uuidBytes.flip();
                 // DJW TODO Specify endianess to check
                 UUID uuid1 = new UUID(uuidBytes.asLongBuffer().get(), uuidBytes.asLongBuffer().get());
                 uuid = uuid1.toString().replace("-", "");
             }
 
             String archName = unknownString;
             if (image.hasCodeType() && image.getCodeType().getEncoding() == TypeEncoding.TYPE_ENCODING_MACH) {
 
                 switch (CpuType.valueOf((int)image.getCodeType().getType())) {
                     case CPU_TYPE_ARM:
                         switch (CpuSubTypeArm.valueOf((int)image.getCodeType().getSubtype())) {
                             case CPU_SUBTYPE_ARM_V6:
                                 archName = "armv6";
                                 break;
                             case CPU_SUBTYPE_ARM_V7:
                                 archName = "armv7";
                                 break;
                             case CPU_SUBTYPE_ARM_V7S:
                                 archName = "armv7s";
                                 break;
                             default:
                                 archName = "arm-unknown";
                         }
                         break;
                     case CPU_TYPE_X86:
                         archName = "i386";
                         break;
                     case CPU_TYPE_X86_64:
                         archName = "x86_64";
                         break;
                     case CPU_TYPE_POWERPC:
                         archName = "powerpc";
                         break;
 
                     default:
                         // Use the default archName value (initialized above).
                         break;
                 }
             }
 
             /* Determine if this is the main executable */
             String binaryDesignator = " ";
             if (image.getName().equals(report.getProcessInfo().getProcessPath()))
                 binaryDesignator = "+";
 
             String fmt = null;
             if(getCpuType().isLp64()) {
                 fmt = "%18#x - 0x%18#x %s%s %s  <%s> %s\n";
             } else {
                 fmt = "%#10x - %#10x %s%s %s  <%s> %s\n";
             }
             sb.append(String.format(fmt,
                     image.getBaseAddress(),
                     image.getBaseAddress() + (Math.max(1, image.getSize())-1),
                     binaryDesignator,
                     getLastPathComponent(image.getName()),
                     archName,
                     uuid,
                     image.getName()
             ));
         }
 
 
         return sb.toString();
     }
 
     private String getExceptionString() {
         StringBuilder sb = new StringBuilder();
         if (report.hasException()) {
             sb.append("Application Specific Information:\n")
               .append(String.format("*** Terminating app due to uncaught exception '%s', reason: '%s'\n",
                     report.getException().getName(), report.getException().getReason()))
               .append("\n");
         }
         return sb.toString();
     }
 
     private String getStackFrameInfo(StackFrame frame, long frameIdx) {
         String imageName = unknownString;
         long baseAddress = 0;
         long pcOffset = 0;
 
         BinaryImage image = getImageForAddress(frame.getPc());
         if (image != null) {
             imageName = getLastPathComponent(image.getName());
             baseAddress = image.getBaseAddress();
             pcOffset = frame.getPc() - baseAddress;
         }
 
         return String.format("%-4d%-36s0x%08x 0x%x + %d\n", frameIdx, imageName, frame.getPc(), baseAddress, pcOffset);
     }
 
     BinaryImage getImageForAddress(long address) {
         for( BinaryImage image : report.getBinaryImagesList()) {
             if (image.getBaseAddress() <= address && address < (image.getBaseAddress() + image.getSize()))
                 return image;
         }
         return null;
     }
 
     private void InitFromByteBuffer(ByteBuffer buffer) throws InvalidProtocolBufferException, IOException
     {
         this.header = PlCrashReportFileHeader.createFromByteBuffer(buffer);
 
         if (!this.header.isValid()) {
             throw new IOException("Invalid Crash Report");
         }
 
         this.report = CrashReport_pb.CrashReport.parseFrom(this.header.getData());
     }
 
     private String getLastPathComponent(String path) {
         return path.substring(path.lastIndexOf('/') + 1);
     }
 
     private String getSignalString() {
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("Exception Type:        %s\n", report.getSignal().getName()))
           .append(String.format("Exception Codes:       %s at 0x%x\n", report.getSignal().getCode(), report.getSignal().getAddress()));
         return sb.toString();
     }
 
     private Thread getCrashedThread() {
         for(Thread thread : report.getThreadsList()) {
             if (thread.getCrashed()) {
                 return thread;
             }
         }
         return null;
     }
 
     private String getCrashFingerPrint() throws NoSuchAlgorithmException {
         MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
 
         StringBuilder sb = new StringBuilder();
 
         Thread thread = getCrashedThread();
         // If a thread crashed use the top 5 stack frames of it
         if (thread != null) {
             for(int i = 0 ; i < Math.min(thread.getFramesCount(), NUM_FINGERPRINT_FRAMES) ; i++) {
                 sb.append(getStackFrameInfo(thread.getFrames(i), i));
             }
         // Otherwise use the signal and exception info
         } else {
             sb.append(getSignalString());
             sb.append(getExceptionString());
         }
 
         messageDigest.update(sb.toString().getBytes());
         return new HexBinaryAdapter().marshal(messageDigest.digest());
 
     }
 
     private PlCrashReportFileHeader header;
     private CrashReport_pb.CrashReport report;
 }
