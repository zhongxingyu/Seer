package com;
 
 import java.util.List;
 import difflib.*;
 
 public class DiffLibrary {
 
 	public Patch recieveFiles(final List<String> firstFileList,
 			final List<String> secondFileList) {
 
 		final Patch patch = DiffUtils.diff(firstFileList, secondFileList);
 		return patch;
 
 	}
 
 }
