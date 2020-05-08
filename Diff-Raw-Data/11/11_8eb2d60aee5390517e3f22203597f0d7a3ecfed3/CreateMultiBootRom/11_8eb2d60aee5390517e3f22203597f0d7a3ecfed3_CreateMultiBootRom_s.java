 package com.fusionx.tilal6991.multiboot;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.text.method.ScrollingMovementMethod;
 import android.widget.TextView;
 
 public class CreateMultiBootRom extends Activity {
     @SuppressLint("SdCardPath")
     private class CreateMultibootRomAsync extends
             AsyncTask<Bundle, String, Void> {
         private final static String dataDir = "/data/data/com.fusionx.tilal6991.multiboot/files/";
         private Bundle bundle;
         private String dataImageName;
         private final String externalPath = Environment
                 .getExternalStorageDirectory().getAbsolutePath();
 
         private final String finalOutdir = externalPath + "/multiboot/";
         private String inputFile;
         private final Runnable mFinish = new Runnable() {
             public void run() {
                 final Intent intent = new Intent(getApplicationContext(),
                         MainActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             }
         };
         private final Handler mHandler = new Handler();
 
         private String romExtractionDir;
 
         private String romName;
 
         private String systemImageName;
 
         private String tempFlashableBootDir;
 
         private final String tempSdCardDir = externalPath + "/tempMultiboot/";
 
         private void cleanup() {
             publishProgress("Cleaning up");
             CommonFunctions.deleteIfExists(tempSdCardDir);
             CommonFunctions.deleteIfExists(dataDir);
             publishProgress("Finished!");
         }
 
         @Override
         protected Void doInBackground(final Bundle... params) {
             bundle = params[0];
             inputFile = Environment.getExternalStorageDirectory()
                     .getAbsolutePath() + "/" + bundle.getString("filename");
             romName = bundle.getString("filename").replace(".zip", "");
             romExtractionDir = tempSdCardDir + romName + "/";
             tempFlashableBootDir = tempSdCardDir + "tempFlashBoot/";
 
             preClean();
 
             publishProgress("Making directories");
             new File(romExtractionDir).mkdirs();
             new File(finalOutdir + "loop-roms").mkdirs();
             new File(finalOutdir + "boot-images").mkdirs();
             new File(tempSdCardDir
                     + "tempFlashBoot/META-INF/com/google/android/").mkdirs();
 
             publishProgress("Getting data from wizard");
             dataImageName = bundle.getString("dataimagename");
             systemImageName = bundle.getString("systemimagename");
 
             final boolean data = bundle.getBoolean("createdataimage");
             final boolean system = bundle.getBoolean("createsystemimage");
 
             if (system)
                 makeSystemImage();
             if (data)
                 makeDataImage();
             extractRom();
             remakeBootImage();
             fixUpdaterScript();
             packUpAndFinish();
             cleanup();
             return null;
         }
 
         private void extractRom() {
            publishProgress("Extracting ROM");
             CommonFunctions.runRootCommand(dataDir + "busybox unzip -q "
                     + inputFile + " -d " + romExtractionDir);
         }
 
         private void findAndReplaceInFile(final String fileName,
                 final String findString, final String replaceString) {
             try {
                 final Scanner scanner = new Scanner(new File(fileName));
                 final FileWriter s = new FileWriter(new File(fileName + ".fix"));
                 while (scanner.hasNextLine()) {
                     final String nextLine = scanner.nextLine();
                     if (nextLine.contains(findString))
                         s.write(replaceString + "\n");
                     else
                         s.write(nextLine + "\n");
                 }
                 s.close();
                 CommonFunctions.runRootCommand("mv " + fileName + ".fix "
                         + fileName);
             } catch (final FileNotFoundException e) {
                 e.printStackTrace();
             } catch (final IOException e) {
                 e.printStackTrace();
             }
         }
 
         private boolean findTextInFile(final String fileName,
                 final String findString) {
             try {
                 final Scanner scanner = new Scanner(new File(fileName));
                 while (scanner.hasNextLine()) {
                     final String nextLine = scanner.nextLine();
                     if (nextLine.contains(findString))
                         return true;
                     else
                         return false;
                 }
             } catch (final FileNotFoundException e) {
                 e.printStackTrace();
             }
             return false;
         }
 
         private void fixUpdaterScript() {
             publishProgress("Editing updater script");
             final String updaterScript = romExtractionDir
                     + "META-INF/com/google/android/updater-script";
             String findString = null;
             try {
                 final Scanner scanner = new Scanner(new File(updaterScript));
                 while (scanner.hasNextLine()) {
                     findString = scanner.nextLine();
                     if (findString.contains("format(")
                             && (findString.contains("\"MTD\", \"system\"")))
                         findAndReplaceInFile(updaterScript, findString, "");
                 }
             } catch (final FileNotFoundException e) {
                 e.printStackTrace();
             }
             findAndReplaceInFile(
                     updaterScript,
                     "mount(\"yaffs2\", \"MTD\", \"system\", \"/system\");",
                     "run_program(\"/sbin/losetup\", \"/dev/block/loop0\", \"/sdcard/multiboot/"
                             + systemImageName
                             + "\");\n"
                             + "run_program(\"/sbin/mke2fs\", \"-T\", \"ext2\", \"/dev/block/loop0\");\n"
                             + "run_program(\"/sbin/mount\", \"-t\", \"ext2\", \"/dev/block/loop0\", \"/system\");");
             findAndReplaceInFile(
                     updaterScript,
                     "unmount(\"/system\");",
                     "unmount(\"/system\");\n"
                             + "run_program(\"/sbin/losetup\", \"-d\", \"/dev/block/loop0\");");
             findAndReplaceInFile(
                     updaterScript,
                     "run_program(\"/sbin/busybox\", \"mount\", \"/system\");",
                     "run_program(\"/sbin/losetup\", \"/dev/block/loop0\", \"/sdcard/multiboot/"
                             + systemImageName
                             + "\");\n"
                             + "run_program(\"/sbin/mke2fs\", \"-T\", \"ext2\", \"/dev/block/loop0\");\n"
                             + "run_program(\"/sbin/mount\", \"-t\", \"ext2\", \"/dev/block/loop0\", \"/system\");");
             findAndReplaceInFile(
                     updaterScript,
                     "run_program(\"/sbin/busybox\", \"umount\", \"/system\");",
                     "unmount(\"/system\");\n"
                             + "run_program(\"/sbin/losetup\", \"-d\", \"/dev/block/loop0\");");
         }
 
         private void makeDataImage() {
            publishProgress("Making data image");
             makeImage(finalOutdir + dataImageName,
                     Integer.parseInt(bundle.getString("dataimagesize")) * 1024);
         }
 
         private void makeImage(final String imageOutput, final int imageSize) {
             final String losetupLocation = CommonFunctions.runRootCommand(
                     "losetup -f").trim();
             CommonFunctions.runRootCommand("dd if=/dev/zero of=" + imageOutput
                     + " bs=1024 count=" + imageSize);
             CommonFunctions.runRootCommand("losetup " + losetupLocation + " "
                     + imageOutput);
             CommonFunctions.runRootCommand("mke2fs -t ext2 " + losetupLocation);
             try {
                 Thread.sleep(10000);
             } catch (final InterruptedException e) {
                 e.getStackTrace();
             }
             CommonFunctions.runRootCommand("losetup -d " + losetupLocation);
         }
 
         private void makeSystemImage() {
            publishProgress("Making system image");
             makeImage(
                     finalOutdir + systemImageName,
                     Integer.parseInt(bundle.getString("systemimagesize")) * 1024);
         }
 
         @Override
         protected void onProgressUpdate(final String... values) {
             super.onProgressUpdate(values);
             WriteOutput(values[0]);
             if (values[0] == "Finished!")
                 mHandler.postDelayed(mFinish, 5000);
         }
 
         private void packUpAndFinish() {
            publishProgress("Making ROM zip");
             CommonFunctions.runRootCommands(new String[] {
                     "cd " + romExtractionDir,
                     dataDir + "zip -r -q " + finalOutdir + "loop-roms/"
                             + romName + "-loopinstall.zip " + "*" });
 
             publishProgress("Creating copy of loop boot image for flashing in recovery");
             CommonFunctions.runRootCommand("cp " + romExtractionDir
                     + "boot.img " + tempFlashableBootDir + "boot.img");
 
             final String updaterScriptFile = "package_extract_file(\"boot.img\", \"/tmp/boot.img\");write_raw_image(\"/tmp/boot.img\", \"boot\");";
 
             publishProgress("Creating flashable boot image in recovery");
             writeToFile(
                     tempSdCardDir
                             + "tempFlashBoot/META-INF/com/google/android/updater-script",
                     updaterScriptFile);
 
             CommonFunctions.runRootCommands(new String[] {
                     "cd " + tempFlashableBootDir,
                     dataDir + "zip -r -q " + finalOutdir + "boot-images/"
                             + romName + "-bootimage.zip " + "*" });
 
             publishProgress("Creating copy of loop boot image for flashing in app");
             CommonFunctions.runRootCommand("cp " + romExtractionDir
                     + "boot.img " + finalOutdir + romName + "boot.img");
 
             String shFile = "#!/system/bin/sh\n"
                     + "flash_image boot /sdcard/multiboot/" + romName
                     + "boot.img\n" + "reboot";
 
             publishProgress("Creating loop script file");
             writeToFile(finalOutdir + "boot" + romName + ".sh", shFile);
 
             CommonFunctions.runRootCommand("cp /init.rc " + tempSdCardDir
                     + "currentRom.init.rc");
 
             if (!findTextInFile(tempSdCardDir + "currentRom.init.rc",
                     "mount ext2 loop@")) {
                 CommonFunctions.deleteIfExists(finalOutdir + "boot.img");
                 CommonFunctions.deleteIfExists(finalOutdir + "boot.sh");
 
                 publishProgress("Creating nand boot image");
                 CommonFunctions
                         .runRootCommand("dd if=/dev/mtd/mtd1 of=/sdcard/multiboot/boot.img bs=4096");
 
                 shFile = "#!/system/bin/sh\n"
                         + "flash_image boot /sdcard/multiboot/boot.img\n"
                         + "reboot";
 
                 publishProgress("Creating nand script file");
                 writeToFile(finalOutdir + "boot.sh", shFile);
             }
         }
 
         private void preClean() {
             publishProgress("Running a preclean");
             CommonFunctions.deleteIfExists(finalOutdir + romName + "boot.img");
             CommonFunctions.deleteIfExists(finalOutdir + "boot" + romName
                     + ".sh");
             CommonFunctions.deleteIfExists(finalOutdir + "loop-roms/" + romName
                     + "-loopinstall.zip");
             CommonFunctions.deleteIfExists(tempSdCardDir);
         }
 
         private void remakeBootImage() {
             publishProgress("Moving boot image");
             CommonFunctions.runRootCommand("cp " + romExtractionDir
                     + "boot.img " + dataDir + "boot.img");
             CommonFunctions.deleteIfExists(romExtractionDir + "boot.img");
 
             publishProgress("Getting boot.img parameters");
             final String base = "0x"
                     + CommonFunctions.runRootCommand(
                             dataDir + "busybox od -A n -h -j 34 -N 2 "
                                     + dataDir + "boot.img").trim() + "0000";
             final String commandLine = "'"
                     + CommonFunctions.runRootCommand(
                             dataDir + "busybox od -A n --strings -j 64 -N 512 "
                                     + dataDir + "boot.img").trim() + "'";
 
             publishProgress("Extracting kernel");
             CommonFunctions.runRootCommands(new String[] { "cd " + dataDir,
                     "./extract-kernel.pl " + dataDir + "boot.img" });
 
             publishProgress("Extracting ramdisk");
             CommonFunctions.runRootCommands(new String[] { "cd " + dataDir,
                     "./extract-ramdisk.pl " + dataDir + "boot.img" });
             CommonFunctions.runRootCommands(new String[] {
                     "cd " + dataDir + "boot.img-ramdisk",
                     "gunzip -c ../boot.img-ramdisk.cpio.gz | cpio -i" });
 
             CommonFunctions.deleteIfExists(dataDir + "boot.img");
 
             CommonFunctions.runRootCommand("cp " + dataDir
                     + "boot.img-ramdisk/init.rc " + tempSdCardDir + "init.rc");
             CommonFunctions
                     .deleteIfExists(dataDir + "boot.img-ramdisk/init.rc");
 
             publishProgress("Editing init.rc");
 
             String externalDir;
 
             if (findTextInFile(romExtractionDir + "system/build.prop",
                     "ro.build.version.release=4.1"))
                 externalDir = "/storage/sdcard0";
             else
                 externalDir = "/sdcard";
 
             findAndReplaceInFile(tempSdCardDir + "init.rc", "on fs", "on fs\n"
                     + "    mkdir -p " + externalDir + "\n"
                     + "    mount vfat /dev/block/mmcblk0p1 " + externalDir);
 
             findAndReplaceInFile(tempSdCardDir + "init.rc",
                     "mount yaffs2 mtd@system /system ro remount",
                     "    mount ext2 loop@" + externalDir + "/multiboot/"
                             + systemImageName + " /system ro remount");
 
             findAndReplaceInFile(tempSdCardDir + "init.rc",
                     "mount yaffs2 mtd@system /system", "    mount ext2 loop@"
                             + externalDir + "/multiboot/" + systemImageName
                             + " /system");
 
             findAndReplaceInFile(tempSdCardDir + "init.rc",
                     "mount yaffs2 mtd@userdata /data nosuid nodev",
                     "    mount ext2 loop@" + externalDir + "/multiboot/"
                             + dataImageName + " /data nosuid nodev");
 
             CommonFunctions.runRootCommand("cp " + tempSdCardDir + "init.rc "
                     + dataDir + "boot.img-ramdisk/init.rc");
             CommonFunctions.deleteIfExists(tempSdCardDir + "init.rc");
 
             publishProgress("Making compressed ramdisk");
             CommonFunctions.runRootCommands(new String[] { "cd " + dataDir,
                     "./mkbootfs boot.img-ramdisk | gzip > ramdisk.gz" });
 
             publishProgress("Making boot image");
             CommonFunctions
                     .runRootCommands(new String[] {
                             "cd " + dataDir,
                             "./mkbootimg --cmdline "
                                     + commandLine
                                     + " --base "
                                     + base
                                     + " --kernel boot.img-kernel --ramdisk ramdisk.gz -o "
                                     + romExtractionDir + "boot.img" });
         }
 
         private void WriteOutput(final String paramString) {
             final TextView editText = (TextView) findViewById(R.id.editText1);
             editText.append(paramString + "\n");
             editText.setMovementMethod(new ScrollingMovementMethod());
         }
 
         private void writeToFile(final String fileName,
                 final String stringToWrite) {
             try {
                 final FileWriter fileWriter = new FileWriter(fileName);
                 fileWriter.write(stringToWrite);
                 fileWriter.close();
             } catch (final IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void onBackPressed() {
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_make_multi_boot);
         final CreateMultibootRomAsync instance = new CreateMultibootRomAsync();
         instance.execute(getIntent().getExtras());
     }
 }
