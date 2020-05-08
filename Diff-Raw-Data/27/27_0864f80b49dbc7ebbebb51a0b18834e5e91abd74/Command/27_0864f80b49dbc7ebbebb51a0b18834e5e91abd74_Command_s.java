 package org.imaginea.botbot;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.res.Resources.NotFoundException;
 import android.view.View;
 
 public class Command {
 	ViewClasses vc = new ViewClasses();
 
 	public Command() {
 
 	}
 
 	String userAction;
 	Object view;
 	List<Object> arguments;
 	int id = 0;
 	String viewClassName = "";
 
 	public void add(String command, Object view, Object... args) {
 		this.userAction = command;
 		this.view = view;
 		if(args.length==1 && args[0].equals("")){
 			this.arguments= new ArrayList<Object>();
 		}
 		else{
 			this.arguments = new ArrayList<Object>(Arrays.asList(args));
 		}
 		if (view.getClass().getName().contains("Button")) {
 			this.userAction = "clickbutton";
 		}
 		if(String.class.isAssignableFrom(view.getClass())){
 			arguments.add(0, (String)view);
 			this.userAction="clicktext";
 		}
 
 	}
 
 	public void add(String command, View view, Object... args) {
 		this.userAction = command;
 		this.view = view;
 		if(args.length==1 && args[0].equals("")){
 			this.arguments= new ArrayList<Object>();
 		}
 		else{
 			this.arguments = new ArrayList<Object>(Arrays.asList(args));
 		}
 		String className = view.getClass().getName();
 		if (vc.isSupportedClass(className)) {
 			this.viewClassName = vc.getFullClassName(className);
 		} else {
 			this.viewClassName = "";
 		}
 
 		this.id = view.getId();
 
 		try {
 			if(arguments.size()>=1){
 				arguments.add(0, getStringIdFromResource(view, id));
 			}else{
 				arguments.add(getStringIdFromResource(view, id));
 			}
 			if (command.contentEquals("click")) {
 				this.userAction = "clickbyid";
 			}
 		} catch (NotFoundException e) {
 			//Escaping in case the rid resource is not found
 		}
 	}
 
 	private String getStringIdFromResource(View view, int id)
 			throws NotFoundException {
 		String rid = "";
 		String tmp = (String) view.getContext().getResources()
 				.getResourceName(id);
 		if (tmp.contains("/")) {
 			rid = tmp.substring(tmp.lastIndexOf("/") + 1);
 		}
 		return rid;
 	}
 
 	public String getData() {
 		String data = "";
 		data = data.concat("\"command\":\"" + this.userAction + "\"");
 		data = data.concat(",\"viewClassName\":\"" + this.viewClassName + "\"");
 		int i=0;
 		for (Object args:arguments) {
 			data = data.concat(",\"args[" + i + "]\":\"" + args + "\"");
 			i++;
 		}
 
 		data = data.replace("\"", "\\\"");
 		data = "{".concat(data).concat("}");
 		return data;
 	}
 
 	public void add(String command) {
 		this.userAction = command;
 		this.arguments= new ArrayList<Object>();
 	}
 
 	@Override
 	public String toString() {
 		String retText;
 		try {
 			retText = "command =" + this.userAction + "; view=" + view.toString()
 					+ "; viewClassName=" + this.viewClassName + "; id=" + id
 					+ "; args[0]=" + arguments.get(0).toString()
 					+ "; args[1]=" + arguments.get(1).toString()
 					+ "; args[2]=" + arguments.get(2).toString();
 		} catch (IndexOutOfBoundsException e) {
 			retText = "command =" + this.userAction + "; view=" + view.toString()
 					+ "; viewClassName=" + this.viewClassName + "; id=" + id
 					+ "; args=" + arguments.toString();
 		}
 		return retText;
 	}
 }
