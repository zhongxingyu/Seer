 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 import java.io.*;
 
 public class ffi2ffi {
     public static void main(String args[]) throws Exception {
	FFITool tool = new FFITool();
         tool.cli("ffi2ffi", args);
     }
 }
