 package org.vfsutils.shell.commands;
 
 import java.beans.BeanDescriptor;
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.operations.FileOperation;
 import org.apache.commons.vfs.operations.FileOperations;
 import org.vfsutils.shell.Arguments;
 import org.vfsutils.shell.CommandException;
 import org.vfsutils.shell.CommandInfo;
 import org.vfsutils.shell.CommandParser;
 import org.vfsutils.shell.CommandProvider;
 import org.vfsutils.shell.Engine;
 
 public class Ops extends AbstractCommand implements CommandProvider {
 
 	protected String listAction = "list";
 	protected String doAction = "do";
 	protected String usageAction = "usage";
 
 	public Ops() {
 		super("ops", new CommandInfo("Interact with operations",
 				"list <file> | do <id|name> <file> | usage <id|name> <file>"));
 	}
 
 	public void execute(Arguments args, Engine engine)
 			throws IllegalArgumentException, CommandException,
 			FileSystemException {
 		
 		args.assertSize(2);
 
 		String action = args.getArgument(0);
 
 		if (action.equals(this.listAction)) {
 			listops(args, engine);
 		} else if (action.equals(this.doAction)) {
 			doop(args, engine);
 		} else if (action.equals(this.usageAction)) {
 			usage(args, engine);
 		} else {
 			throw new IllegalArgumentException("Unkown action " + action);
 		}
 
 	}
 
 	protected void doop(Arguments args, Engine engine)
 			throws IllegalArgumentException, CommandException,
 			FileSystemException {
 
 		if (args.getArguments().size() < 3) {
 			throw new IllegalArgumentException("Not enough arguments");
 		}
 
 		String op = args.getArgument(1);
 		String path = args.getArgument(2);
 
 		final FileObject[] files = engine.pathToFiles(path);
 
 		if (files.length == 0) {
 			engine.error("File does not exist: " + path);
 		}
 
 		for (int i = 0; i < files.length; i++) {
 			FileObject file = files[i];
 			// check if the second argument is an int
 			try {
 				int index = Integer.parseInt(op);
 				doop(file, index, args, engine);
 			} catch (NumberFormatException e) {
 				// must be a name
 				doop(file, op, args, engine);
 			}
 		}
 
 	}
 
 	protected void doop(FileObject file, int index, Arguments args,
 			Engine engine) throws CommandException, FileSystemException {
 		FileOperations fops = file.getFileOperations();
 		Class[] ops = fops.getOperations();
 		// counting starts at 1
 		if (index > 0 && index <= ops.length) {
 			FileOperation op = fops.getOperation(ops[index - 1]);
 			doop(file, op, args, engine);
 		} else {
 			engine.println("Operation at index " + index + " does not exist");
 		}
 	}
 
 	protected void doop(FileObject file, String displayname, Arguments args,
 			Engine engine) throws FileSystemException, CommandException {
 
 		try {
 			FileOperations fops = file.getFileOperations();
 			Class[] ops = fops.getOperations();
 
 			Class opClass = nameToOperationClass(ops, displayname);
 
 			if (opClass == null) {
 				engine.println("Could not find operation " + displayname);
 			} else {
 				FileOperation op = fops.getOperation(opClass);
 				doop(file, op, args, engine);
 			}
 		} catch (Exception e) {
			throw new CommandException("Error: " + e.getMessage(), e);
 		}
 	}
 
 	protected void doop(FileObject file, FileOperation op, Arguments args, Engine engine)
 			throws FileSystemException, CommandException {
 
 		try {
 			// apply the options
 			this.setOptions(op, args);
 
 			// process
 			op.process();
 
 			// some dirty stuff to get around the no-feedback limitation
 			BeanInfo binfo = java.beans.Introspector.getBeanInfo(op.getClass());
 
 			PropertyDescriptor[] props = binfo.getPropertyDescriptors();
 
 			for (int i = 0; i < props.length; i++) {
 				PropertyDescriptor p = props[i];
 				if (p.getName().equals("result") && p.getReadMethod() != null) {
 					Method readMethod = p.getReadMethod();
 					Object result = readMethod.invoke(op, null);
 					engine.println("Operation result for " + engine.getCwd().getName().getRelativeName(file.getName()) + ":");
 					if (result == null) {
 						engine.println("n/a");
 					} else {
 						engine.println(result.toString());
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			throw new CommandException(e);
 		}
 	}
 
 	protected Class nameToOperationClass(Class[] ops, String displayname)
 			throws IntrospectionException {
 		Class opClass = null;
 
 		for (int i = 0; i < ops.length; i++) {
 			Class o = ops[i];
 			BeanInfo binfo = java.beans.Introspector.getBeanInfo(o);
 			if (binfo.getBeanDescriptor().getDisplayName().equalsIgnoreCase(
 					displayname)) {
 				opClass = o;
 				break;
 			}
 		}
 
 		return opClass;
 	}
 
 	protected void listops(Arguments args, Engine engine)
 			throws IllegalArgumentException, CommandException, FileSystemException {
 		if (args.getArguments().size() < 2) {
 			throw new IllegalArgumentException("Not enough arguments");
 		}
 
 		String path = args.getArgument(1);
 
 		final FileObject file = engine.pathToExistingFile(path);
 		
 		listops(file, engine);
 
 	}
 
 	protected void listops(FileObject file, Engine engine)
 			throws FileSystemException, CommandException {
 		
 		Class[] operations = file.getFileOperations().getOperations();
 		if (operations != null) {
 			try {
 				for (int i = 0; i < operations.length; i++) {
 					BeanInfo binfo = java.beans.Introspector
 							.getBeanInfo(operations[i]);
 					engine.println("[" + (i + 1) + "] "
 							+ binfo.getBeanDescriptor().getDisplayName());
 				}
 			} catch (IntrospectionException e) {
 				throw new CommandException (
 						"Error listing the operations", e);
 			}
 		}
 	}
 
 	protected void usage(Arguments args, Engine engine)
 			throws CommandException, FileSystemException {
 
 		if (args.getArguments().size() < 3) {
 			throw new IllegalArgumentException("Not enough arguments");
 		}
 
 		String op = args.getArgument(1);
 		String path = args.getArgument(2);
 
 		final FileObject file = engine.pathToExistingFile(path);
 
 		// check if the second argument is an int
 		try {
 			int index = Integer.parseInt(op);
 			usage(file, index, engine);
 		} catch (NumberFormatException e) {
 			// must be a name
 			usage(file, op, engine);
 		}
 
 	}
 
 	protected void usage(FileObject file, String displayname, Engine engine)
 			throws FileSystemException, CommandException {
 		try {
 			FileOperations fops = file.getFileOperations();
 			Class[] ops = fops.getOperations();
 
 			Class opClass = nameToOperationClass(ops, displayname);
 
 			if (opClass == null) {
 				throw new IllegalArgumentException("Could not find operation "
 						+ displayname);
 			} else {
 				usage(opClass, engine);
 			}
 		} catch (IntrospectionException e) {
 			throw new CommandException("Could not get usage of operation "
 					+ displayname, e);
 		}
 	}
 
 	protected void usage(FileObject file, int index, Engine engine)
 			throws FileSystemException, CommandException {
 
 		try {
 			FileOperations fops = file.getFileOperations();
 			Class[] ops = fops.getOperations();
 
 			// counting starts at 1
 			if (index > 0 && index <= ops.length) {
 				usage(ops[index - 1], engine);
 			} else {
 				throw new IllegalArgumentException("Operation at index "
 						+ index + " does not exist");
 			}
 		} catch (IntrospectionException e) {
 			throw new CommandException(
 					"Error showing usage of operation at index " + index, e);
 		}
 	}
 
 	protected void usage(Class opClass, Engine engine)
 			throws IntrospectionException {
 
 		BeanInfo binfo = java.beans.Introspector.getBeanInfo(opClass);
 		BeanDescriptor bnd = binfo.getBeanDescriptor();
 		PropertyDescriptor[] props = binfo.getPropertyDescriptors();
 
 		engine.println(bnd.getDisplayName() + ": " + bnd.getShortDescription());
 		engine.println("options:");
 		for (int i = 0; i < props.length; i++) {
 			PropertyDescriptor p = props[i];
 			if (p.getWriteMethod() != null) {
 				engine.println("  --" + p.getDisplayName() + ": "
 						+ p.getShortDescription());
 			}
 		}
 	}
 
 	protected void setOptions(FileOperation op, Arguments args)
 			throws Exception {
 		Class opClass = op.getClass();
 		BeanInfo binfo = java.beans.Introspector.getBeanInfo(opClass);
 
 		PropertyDescriptor[] props = binfo.getPropertyDescriptors();
 
 		for (int i = 0; i < props.length; i++) {
 			PropertyDescriptor p = props[i];
 			if (p.getWriteMethod() != null) {
 				// check if it exists in the arguments
 				String name = p.getName();
 				if (args.hasOption(name)) {
 					Method writeMethod = p.getWriteMethod();
 					writeMethod.invoke(op,
 							new Object[] { args.getOption(name) });
 				}
 			}
 		}
 	}
 
 }
