 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.areadetector.v17.impl;
 
 import gda.device.detector.areadetector.v17.NDFile;
 import gda.device.detector.areadetector.v17.NDPluginBase;
 
 /**
  * Simulation of NDFile for testing.
  */
 public class NDFileSimulator implements NDFile{
 
 	NDPluginBase pluginBase;
 
 	public NDFileSimulator(NDPluginBase pluginBase){
 		this.pluginBase = pluginBase;
 	}
 	public NDFileSimulator(){
 	}
 
 	@Override
 	public NDPluginBase getPluginBase() {
 		return pluginBase;
 	}	
 	
 	String filePath="";
 	@Override
 	public String getFilePath() throws Exception {
 		return filePath;
 	}
 
 	@Override
 	public void setFilePath(String filepath) throws Exception {
 		this.filePath = filepath;
 	}
 
 	String fileName="";
 
 	int fileNumber=0;
 
 
 	short autoIncrement=0;
 
 	String fileTemplate="";
 
 	@Override
 	public String getFullFileName_RBV() throws Exception { 
		return "/scratch/1.tif";//fileTemplate; //String.format(fileTemplate, getFilePath(), getFileName(), getFileNumber());
 	}
 
 	@Override
 	public String getFileName() {
 		return fileName;
 	}
 
 	@Override
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	@Override
 	public int getFileNumber() {
 		return fileNumber;
 	}
 
 	@Override
 	public void setFileNumber(int fileNumber) {
 		this.fileNumber = fileNumber;
 	}
 
 	@Override
 	public short getAutoIncrement() {
 		return autoIncrement;
 	}
 
 	@Override
 	public String getFileTemplate() {
 		return fileTemplate;
 	}
 
 	@Override
 	public void setFileTemplate(String fileTemplate) {
 		this.fileTemplate = fileTemplate;
 	}
 
 	@Override
 	public short getAutoSave() {
 		return autoSave;
 	}
 
 	public void setAutoSave(short autoSave) {
 		this.autoSave = autoSave;
 	}
 
 	@Override
 	public short getWriteFile() {
 		return writeFile;
 	}
 
 	@Override
 	public int getStatus() {
 		return status;
 	}
 
 	@Override
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	short autoSave=0;
 
 	@Override
 	public short getAutoSave_RBV() throws Exception {
 		return autoSave;
 	}
 
 	short writeFile=0;
 
 	@Override
 	public short getWriteFile_RBV() throws Exception {
 		return writeFile;
 	}
 
 	short readFile=0;
 	@Override
 	public short getReadFile() throws Exception {
 		return readFile;
 	}
 
 	@Override
 	public void setReadFile(int readfile) throws Exception {
 		this.readFile = (short)readfile;
 	}
 
 	@Override
 	public short getReadFile_RBV() throws Exception {
 		return readFile;
 	}
 
 	short fileFormat=0;
 	@Override
 	public short getFileFormat() throws Exception {
 		return fileFormat;
 	}
 
 	@Override
 	public void setFileFormat(int fileformat) throws Exception {
 		fileFormat = (short)fileformat;
 	}
 
 	@Override
 	public short getFileFormat_RBV() throws Exception {
 		return fileFormat;
 	}
 
 	short fileWriteMode=0;
 	@Override
 	public short getFileWriteMode() throws Exception {
 		return fileWriteMode;
 	}
 
 	@Override
 	public void setFileWriteMode(int filewritemode) throws Exception {
 		fileWriteMode = (short)filewritemode;
 		
 	}
 
 	@Override
 	public short getFileWriteMode_RBV() throws Exception {
 		return fileWriteMode;
 	}
 
 	short capture=0;
 	@Override
 	public short getCapture() throws Exception {
 		return capture;
 	}
 
 	@Override
 	public void startCapture() throws Exception {
 		capture = 1; //?
 	}
 
 	@Override
 	public short getCapture_RBV() throws Exception {
 		return capture;
 	}
 
 	int numCapture=0;
 	@Override
 	public int getNumCapture() throws Exception {
 		return numCapture;
 	}
 
 	@Override
 	public void setNumCapture(int numcapture) throws Exception {
 		numCapture = numcapture;
 	}
 
 	@Override
 	public int getNumCapture_RBV() throws Exception {
 		return numCapture;
 	}
 
 	@Override
 	public int getNumCaptured_RBV() throws Exception {
 		return numCapture;
 	}
 
 	String initialFileName="";
 	@Override
 	public String getInitialFileName() {
 		return initialFileName;
 	}
 
 	@Override
 	public void reset() throws Exception {
 		
 	}
 
 	@Override
 	public void stopCapture() throws Exception {
 		capture=0;
 	}
 
 	int status=0;
 
 	@Override
 	public void getEPICSStatus() throws Exception {
 		//?
 	}
 
 	@Override
 	public String getInitialFileTemplate() {
 		return fileTemplate;
 	}
 
 	@Override
 	public void startCaptureSynchronously() throws Exception {
 		//
 	}
 
 	@Override
 	public void resetFileTemplate() throws Exception {
 		//
 	}
 
 	@Override
 	public void setFileWriteMode(gda.device.detector.areadetector.v17.NDFile.FileWriteMode mode) throws Exception {
 	}
 
 	@Override
 	public String getFilePath_RBV() throws Exception {
 		return getFilePath();
 	}
 
 	@Override
 	public String getFileName_RBV() throws Exception {
 		return getFileName();
 	}
 
 	@Override
 	public int getFileNumber_RBV() throws Exception {
 		return getFileNumber();
 	}
 
 	@Override
 	public void setAutoIncrement(int autoincrement) throws Exception {
 		autoIncrement = (short)autoincrement;
 		
 	}
 
 	@Override
 	public short getAutoIncrement_RBV() throws Exception {
 		return autoIncrement;
 	}
 
 	@Override
 	public String getFileTemplate_RBV() throws Exception {
 		return fileTemplate;
 	}
 
 	@Override
 	public void setAutoSave(int autosave) throws Exception {
 		autosave= (short)autosave;
 		
 	}
 
 	@Override
 	public void setWriteFile(int writefile) throws Exception {
 		writeFile = (short)writefile;
 		
 	}
 
 	@Override
 	public void waitWhileStatusBusy() throws InterruptedException {
 	}
 
 
 }
