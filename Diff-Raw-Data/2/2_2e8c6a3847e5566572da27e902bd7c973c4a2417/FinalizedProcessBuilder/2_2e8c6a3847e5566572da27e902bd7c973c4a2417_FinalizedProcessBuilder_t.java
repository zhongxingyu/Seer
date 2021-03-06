 package com.leacox.process;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * This class wraps {@link ProcessBuilder} for creating operating system
  * processes using the safer {@link FinalizedProcess}.
  * 
  * <p>
  * Like {@code ProcessBuilder}, each @{code FinalizedProcessBuilder} instances
  * manages a collection of process attributes. The {@link #start()} method
  * creates a new {@link FinalizedProcess} instance with this attributes. The
  * {@link #start()} method can be invoked multiple times from the same instance
  * to create new subprocesses with identical or related attributes.
  * 
  * <p>
  * Each process builder manages the following attributes, in addition to the
  * attributes from {@link ProcessBuilder}:
  * 
  * 
  * <ul>
  * 
  * <li>a <i>keepProcess</i> indicator, a boolean indicator as to whether the
  * process should be destroyed during cleanup or not. By default the process
  * will be destroyed during cleanup.</li>
  * 
  * </ul>
  * 
  * <p>
  * Starting a new process with the default attributes is just as easy as
  * {@link ProcessBuilder}:
  * 
  * <pre>
  * {@code FinalizedProcess process = new ProcessBuilder("myCommand", "myArg").start();}
  * </pre>
  * 
  * Here is an example that redirects standard error to standard output and does
  * not destroy the process when the process is closed:
  * 
  * <pre>
  * {@code
  * FinalizedProcessBuilder pb = new FinalizedProcessBuilder("myCommand", "myArg");
  * pb.redirectErrorStream(true);
  * pb.keepProcess();
  * FinalizedProcess process = pb.start();
  * }
  * </pre>
  * 
  * Note: For additional documentation see {@link ProcessBuilder}.
  * <p>
  * 
  * @author John Leacox
  * 
  */
 public class FinalizedProcessBuilder {
 	private final ProcessBuilder processBuilder;
 
 	private boolean keepProcess = false;
 
 	/**
 	 * Constructs a process builder with the specified operating system program
 	 * and arguments. This constructor does <i>not</i> make a copy of the
 	 * {@code command} list. Subsequent updates to the list will be reflected in
 	 * the state of the process builder. It is not checked whether
 	 * {@code command} corresponds to a valid operating system command.
 	 * 
 	 * @param command
 	 *            the list containing the program and its arguments (cannot be
 	 *            null)
 	 * @throws NullPointerException
 	 *             if command is null
 	 */
 	public FinalizedProcessBuilder(List<String> command) {
 		if (command == null) {
 			throw new NullPointerException();
 		}
 		this.processBuilder = new ProcessBuilder(command);
 	}
 
 	/**
 	 * Constructs a process builder with the specified operating system program
 	 * and arguments. This is a convenience constructor that sets the process
 	 * builder's command to a string list containing the same strings as the
 	 * command array, in the same order. It is not checked whether command
 	 * corresponds to a valid operating system command.
 	 * 
 	 * @param command
 	 *            a string array containing the program and its arguments
 	 *            (cannot be null)
 	 * @throws NullPointerException
	 *             if command is null
 	 */
 	public FinalizedProcessBuilder(String... command) {
 		if (command == null) {
 			throw new NullPointerException("command: null");
 		}
 		this.processBuilder = new ProcessBuilder(command);
 	}
 
 	/**
 	 * Sets this process builder's operating system program and arguments. This
 	 * method does <i>not</i> make a copy of the {@code command} list.
 	 * Subsequent updates to the list will be reflected in the state of the
 	 * process builder. It is not checked whether {@code command} corresponds to
 	 * a valid operating system command.
 	 * 
 	 * @param command
 	 *            the list containing the program and its arguments (cannot be
 	 *            null)
 	 * @return this process builder
 	 * 
 	 * @throws NullPointerException
 	 *             if the command is null
 	 */
 	public FinalizedProcessBuilder command(List<String> command) {
 		processBuilder.command(command);
 		return this;
 	}
 
 	/**
 	 * Sets this process builder's operating system program and arguments. This
 	 * is a convenience method that sets the command to a string list containing
 	 * the same strings as the {@code command} array, in the same order. It is
 	 * not checked whether {@code command} corresponds to a valid operating
 	 * system command.
 	 * 
 	 * @param command
 	 *            a string array containing the program and its arguments
 	 *            (cannot be null)
 	 * @return this process builder
 	 * @throws NullPointerException
 	 *             if command is null
 	 */
 	public FinalizedProcessBuilder command(String... command) {
 		if (command == null) {
 			throw new IllegalArgumentException("command: null");
 		}
 		processBuilder.command(command);
 		return this;
 	}
 
 	public FinalizedProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
 		processBuilder.redirectErrorStream(redirectErrorStream);
 		return this;
 	}
 
 	public FinalizedProcessBuilder directory(File file) {
 		processBuilder.directory(file);
 		return this;
 	}
 
 	public FinalizedProcessBuilder inheritIO() {
 		processBuilder.inheritIO();
 		return this;
 	}
 
 	/**
 	 * Sets the process builder to keep the process when it is cleaned up via
 	 * the {@code FinalizedProcess#close()} method.
 	 * <p>
 	 * 
 	 * By default when the {@code FinalizedProcess} is closed the
 	 * {@link Process#destroy()} method will be called. Calling this method
 	 * indicates that the process should not be destroyed when closed.
 	 * 
 	 * @return this process builder
 	 */
 	public FinalizedProcessBuilder keepProcess() {
 		keepProcess = true;
 		return this;
 	}
 
 	/**
 	 * Starts a new process using the attributes of this process builder.
 	 * 
 	 * <p>
 	 * The new process will invoke the command and arguments given by
 	 * {@link #command()}, in a working directory as given by
 	 * {@link #directory()}, with a process environment as given by
 	 * {@link #environment()}.
 	 * 
 	 * <p>
 	 * This method checks that the command is a valid operating system command.
 	 * Which commands are valid is system-dependent, but at the very least the
 	 * command must be a non-empty list of non-null strings.
 	 * 
 	 * <p>
 	 * A minimal set of system dependent environment variables may be required
 	 * to start a process on some operating systems. As a result, the subprocess
 	 * may inherit additional environment variable settings beyond those in the
 	 * process builder's {@link #environment()}.
 	 * 
 	 * <p>
 	 * If there is a security manager, its {@link SecurityManager#checkExec
 	 * checkExec} method is called with the first component of this object's
 	 * {@code command} array as its argument. This may result in a
 	 * {@link SecurityException} being thrown.
 	 * 
 	 * <p>
 	 * Starting an operating system process is highly system-dependent. Among
 	 * the many things that can go wrong are:
 	 * <ul>
 	 * <li>The operating system program file was not found.
 	 * <li>Access to the program file was denied.
 	 * <li>The working directory does not exist.
 	 * </ul>
 	 * 
 	 * <p>
 	 * In such cases an exception will be thrown. The exact nature of the
 	 * exception is system-dependent, but it will always be a subclass of
 	 * {@link IOException}.
 	 * 
 	 * <p>
 	 * Subsequent modifications to this process builder will not affect the
 	 * returned {@link FinalizedProcess}.
 	 * 
 	 * @return a new {@link FinalizedProcess} object for managing the subprocess
 	 * @throws NullPointerException
 	 *             if an element of the command list is null *
 	 * @throws IndexOutOfBoundsException
 	 *             if the command is an empty list (has size {@code 0}) *
 	 * @throws SecurityException
 	 *             if a security manager exists and
 	 *             <ul>
 	 * 
 	 *             <li>its {@link SecurityManager#checkExec checkExec} method
 	 *             doesn't allow creation of the subprocess, or
 	 * 
 	 *             <li>the standard input to the subprocess was
 	 *             {@linkplain #redirectInput redirected from a file} and the
 	 *             security manager's {@link SecurityManager#checkRead
 	 *             checkRead} method denies read access to the file, or
 	 * 
 	 *             <li>the standard output or standard error of the subprocess
 	 *             was {@linkplain #redirectOutput redirected to a file} and the
 	 *             security manager's {@link SecurityManager#checkWrite
 	 *             checkWrite} method denies write access to the file
 	 * 
 	 *             </ul>
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public FinalizedProcess start() throws IOException {
 		Process process = processBuilder.start();
 		return new FinalizedProcess(process, keepProcess);
 	}
 
 }
