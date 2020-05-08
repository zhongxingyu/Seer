 package com.github.ashtonkem.command;
 
 import java.io.File;
 
 import com.github.ashtonkem.configuration.SourceLayout;
 
 
 public abstract class LispCommand implements Iterable<String> {
 	
 	protected boolean finalize;
 	
 	public LispCommand(boolean finalCommand)
 	{
 		this.finalize = finalCommand;
 	}
 	
 	public boolean finalCommand() 
 	{
 		return finalize;
 	}
 	
 	public abstract void addExpression(String exp);
 	
 	public abstract void addPackage(File f);
 	
 	public abstract void addPackage(String s);
 	
 	public abstract void setLayout(SourceLayout layout);
 	
 	public abstract void setCoreName(String s);
 	
 	public abstract void setMainPackage(String s);
 }
