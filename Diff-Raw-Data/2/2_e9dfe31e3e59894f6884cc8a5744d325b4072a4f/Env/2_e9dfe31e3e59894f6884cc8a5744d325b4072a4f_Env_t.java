 package com.koujalgi.shell.commands;
 
 import com.koujalgi.shell.core.AbstractCommand;
 import com.koujalgi.shell.core.EnvironmentVariable;
 
 public class Env extends AbstractCommand {
 
 	public Env(String baseCommand, int params) {
 		super(baseCommand, params);
 	}
 
 	@Override
 	public String getUsage() {
 		return "env [env var name]";
 	}
 
 	@Override
 	public void execute() {
 		Object var = super.getCommandParser().getParams().get(0);
 		if (var.equals("$")) {
 			for (EnvironmentVariable v : super.getAllEnvVars()) {
 				System.out.println(v.getVariable() + "=" + v.getValue());
 			}
 		} else {
 			EnvironmentVariable env = super.getEnvVar(var);
 			if (env == null) {
 				System.out.println("env var '" + var + "' not set");
 			} else {
 				System.out.println(env.getVariable() + "=" + env.getValue());
 			}
 		}
 	}
 
 	@Override
 	public String getDescription() {
		return "gets a specific env var \n\t\t(Or all env vars if param is '$')";
 	}
 
 }
