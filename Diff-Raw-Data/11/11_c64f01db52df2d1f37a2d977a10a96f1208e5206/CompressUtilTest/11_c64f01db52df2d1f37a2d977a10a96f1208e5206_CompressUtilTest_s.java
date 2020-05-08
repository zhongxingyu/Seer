 package com.madgag.compress;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.InputStream;
 
 import static com.madgag.compress.CompressUtil.unzip;
 import static java.lang.System.currentTimeMillis;
 import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
 import static org.apache.commons.io.FileUtils.getTempDirectory;
 import static org.apache.commons.io.FileUtils.isFileNewer;
 import static org.apache.commons.io.FileUtils.openInputStream;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class CompressUtilTest {
 
     @Test
     public void shouldDecompressAndUnpackZipFileStreamToDestinationFolder() throws Exception {
         File destinationFolder = new File(getTempDirectory(),"unpack-test/"+ currentTimeMillis());
         InputStream zipFileStream=getClass().getResourceAsStream("/util-compress-test-archive.zip");
 
         unzip(zipFileStream, destinationFolder);
 
         assertThat(md5Hex(openInputStream(new File(destinationFolder, "util-compress-test-archive/alpha/beta.txt"))),is("fcaa3361b73b86f17f190ca893601567"));
     }
 }
