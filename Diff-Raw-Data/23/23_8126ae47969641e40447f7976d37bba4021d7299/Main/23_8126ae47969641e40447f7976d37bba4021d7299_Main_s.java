 /*
 * Copyright 2012 Henner Peters
  * Copyright 2012 Uri Shaked
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.swt_release_fetcher;
 
 import java.io.File;
 import java.net.URL;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 public class Main {
	private final static boolean SURPRESS_WARNINGS = false;

 	public static void main(String[] args) throws Exception {

		// suppress htmlUnit warnings
		if (SURPRESS_WARNINGS) {
			System.getProperties().put("org.apache.commons.logging.simplelog.defaultlog", "error");
		}

 		// mirror that we use for all following downloads
 		String mirrorUrl = "";
 
 		// lightweight headless browser
 		WebDriver driver = new HtmlUnitDriver();
 
 		// get swt main site
 		driver.get("http://www.eclipse.org/swt/");
 
 		// find the stable release branch link and hit it
 		List<WebElement> elements = driver.findElements(By.linkText("Linux"));
 		final String deeplink = elements.get(0).getAttribute("href");
 		// System.out.println(deeplink);
 		driver.get(deeplink);
 
 		// get the direct download link from the next page
 		WebElement directDownloadLink = driver.findElement(By.linkText("Direct link to file"));
 		// System.out.println(directDownloadLink.getAttribute("href"));
 
 		// the direct link again redirects, here is our final download link!
 		driver.get(directDownloadLink.getAttribute("href"));
 		final String finalDownloadLink = driver.getCurrentUrl();
 
 		// Close the browser
 		driver.quit();
 
		// extract the mirror URL for all follwing downloads
 		String[] foo = finalDownloadLink.split("\\/", 0);
 		final String filename = foo[foo.length - 1];
 		mirrorUrl = (String) finalDownloadLink.subSequence(0, finalDownloadLink.length() - filename.length());
 		// debug output
		System.out.println("full download url: " + finalDownloadLink);
		System.out.println("mirror url: " + mirrorUrl);
 
 		// determine current release name
 		String[] releaseName = filename.split("-gtk-linux-x86.zip");
 		String versionName = releaseName[0].split("-")[1];
 		System.out.println("current swt version: " + versionName);
 
 		PackageInfo[] packages = {
 				// Win32
 				new PackageInfo("win32-win32-x86.zip", "org.eclipse.swt.win32.win32.x86"),
 				new PackageInfo("win32-win32-x86_64.zip", "org.eclipse.swt.win32.win32.x86_64"),
 				// Linux
 				new PackageInfo("gtk-linux-ppc64.zip", "org.eclipse.swt.gtk.linux.ppc64"),
 				new PackageInfo("gtk-linux-x86.zip", "org.eclipse.swt.gtk.linux.x86"),
 				new PackageInfo("gtk-linux-x86_64.zip", "org.eclipse.swt.gtk.linux.x86_64"),
 				// OSX
 				new PackageInfo("cocoa-macosx.zip", "org.eclipse.swt.cocoa.macosx"),
 				new PackageInfo("cocoa-macosx-x86_64.zip", "org.eclipse.swt.cocoa.macosx.x86_64"),
 				// Additional platforms
 				new PackageInfo("gtk-aix-ppc.zip", "org.eclipse.swt.gtk.aix.ppc"),
 				new PackageInfo("gtk-aix-ppc64.zip", "org.eclipse.swt.gtk.aix.ppc64"),
 				new PackageInfo("gtk-hpux-ia64_32.zip", "org.eclipse.swt.gtk.hpux.ia64_32"),
 				new PackageInfo("gtk-solaris-sparc.zip", "org.eclipse.swt.gtk.solaris.sparc"),
 				new PackageInfo("gtk-solaris-x86.zip", "org.eclipse.swt.gtk.solaris.x86") };
 
 		File downloadDir = new File("downloads");
 		if (!downloadDir.exists()) {
 			downloadDir.mkdirs();
 		}
 
 		for (PackageInfo pkg : packages) {
 			String zipFileName = releaseName[0] + "-" + pkg.zipName;
 			URL downloadUrl = new URL(mirrorUrl + zipFileName);
 			URL checksumUrl = new URL(mirrorUrl + "checksum/" + zipFileName + ".md5");
 
 			System.out.print("* Downloading " + pkg.zipName + " ... ");
 			Artifact artifact = new Artifact(new File(downloadDir, zipFileName), versionName, pkg.artifactId);
 			artifact.downloadAndValidate(downloadUrl, checksumUrl);
 
 			if (artifact.isNewDownload()) {
 				artifact.deploy();
 			}
 		}
 	}
 }
