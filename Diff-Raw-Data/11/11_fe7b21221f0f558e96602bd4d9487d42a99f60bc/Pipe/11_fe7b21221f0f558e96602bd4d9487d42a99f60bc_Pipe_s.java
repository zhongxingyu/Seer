 package pleocmd.pipe;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import pleocmd.Log;
 import pleocmd.exc.ConverterException;
 import pleocmd.exc.InputException;
 import pleocmd.exc.OutputException;
 import pleocmd.exc.PipeException;
 import pleocmd.exc.StateException;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 /**
  * The central processing point of {@link Data} objects.
  * 
  * @author oliver
  */
 public final class Pipe extends StateHandling {
 
 	private final List<Input> inputList = new ArrayList<Input>();
 
 	private final List<Output> outputList = new ArrayList<Output>();
 
 	private final List<Converter> converterList = new ArrayList<Converter>();
 
 	private final Set<Converter> ignoredConverter = new HashSet<Converter>();
 
 	private final Set<Output> ignoredOutputs = new HashSet<Output>();
 
 	private final Set<Long> deadlockDetection = new HashSet<Long>();
 
 	private final DataQueue dataQueue = new DataQueue();
 
 	private int inputPosition;
 
 	private Thread thrInput;
 
 	private Thread thrOutput;
 
 	/**
 	 * Creates a new {@link Pipe}.
 	 */
 	public Pipe() {
 		constructed();
 	}
 
 	/**
 	 * @return an unmodifiable list of all currently connected {@link Input}s
 	 */
 	public List<Input> getInputList() {
 		return Collections.unmodifiableList(inputList);
 	}
 
 	/**
 	 * @return an unmodifiable list of all currently connected {@link Output}s
 	 */
 	public List<Output> getOutputList() {
 		return Collections.unmodifiableList(outputList);
 	}
 
 	/**
 	 * @return an unmodifiable list of all currently connected {@link Converter}
 	 */
 	public List<Converter> getConverterList() {
 		return Collections.unmodifiableList(converterList);
 	}
 
 	/**
 	 * Adds a new {@link Input} to the list of currently connected ones.
 	 * 
 	 * @param input
 	 *            the new {@link Input}
 	 * @throws StateException
 	 *             if the {@link Pipe} is being constructed or already
 	 *             initialized
 	 */
 	public void addInput(final Input input) throws StateException {
 		ensureConstructed();
 		Log.detail("Connecting pipe with input '%s'", input);
 		inputList.add(input);
 	}
 
 	/**
 	 * Adds a new {@link Converter} to the list of currently connected ones.
 	 * 
 	 * @param output
 	 *            the new {@link Converter}
 	 * @throws StateException
 	 *             if the {@link Pipe} is being constructed or already
 	 *             initialized
 	 */
 	public void addOutput(final Output output) throws StateException {
 		ensureConstructed();
 		Log.detail("Connecting pipe with output '%s'", output);
 		outputList.add(output);
 	}
 
 	/**
 	 * Adds a new {@link Output} to the list of currently connected ones.
 	 * 
 	 * @param converter
 	 *            the new {@link Output}
 	 * @throws StateException
 	 *             if the {@link Pipe} is being constructed or already
 	 *             initialized
 	 */
 	public void addConverter(final Converter converter) throws StateException {
 		ensureConstructed();
 		Log.detail("Connecting pipe with converter '%s'", converter);
 		converterList.add(converter);
 	}
 
 	@Override
 	protected void configure0() throws PipeException {
 		Log.detail("Configuring all input");
 		for (final Input in : inputList)
 			in.configure();
 		Log.detail("Configuring all converter");
 		for (final Converter cvt : converterList)
 			cvt.configure();
 		Log.detail("Configuring all output");
 		for (final Output out : outputList)
 			out.configure();
 	}
 
 	@Override
 	protected void init0() throws PipeException {
 		Log.detail("Initializing all input");
 		for (final Input in : inputList)
 			in.init();
 		Log.detail("Initializing all converter");
 		for (final Converter cvt : converterList)
 			cvt.init();
 		Log.detail("Initializing all output");
 		for (final Output out : outputList)
 			out.init();
 	}
 
 	@Override
 	protected void close0() throws PipeException {
 		synchronized (this) {
 			if (thrInput != null || thrOutput != null)
 				throw new PipeException(this, false,
 						"Background threads are still alive");
 		}
 		Log.detail("Closing all input");
 		for (int i = inputPosition; i < inputList.size(); ++i)
 			inputList.get(i).tryClose();
 		Log.detail("Closing all converter");
 		for (final PipePart pp : converterList)
 			if (!ignoredConverter.contains(pp)) pp.tryClose();
 		Log.detail("Closing all output");
 		for (final PipePart pp : outputList)
 			if (!ignoredOutputs.contains(pp)) pp.tryClose();
 		inputPosition = 0;
 		ignoredConverter.clear();
 		ignoredOutputs.clear();
 	}
 
 	/**
 	 * Starts two threads which pipe all data of all connected {@link Input}s
 	 * through the all connected {@link Converter} to all connected
 	 * {@link Output}s.<br>
 	 * Waits until both threads have finished.
 	 * 
 	 * @throws PipeException
 	 *             if the object is not already initialized
 	 * @throws InterruptedException
 	 *             if any thread has interrupted the current thread while
 	 *             waiting for the two pipe threads
 	 */
 	public void pipeAllData() throws PipeException, InterruptedException {
 		ensureInitialized();
 		dataQueue.resetCache();
 		thrInput = new Thread() {
 			@Override
 			public void run() {
 				try {
 					runInputThread();
 				} catch (final Throwable t) { // CS_IGNORE
 					Log.error(t, "Input-Thread died");
 				}
 			}
 		};
 		thrOutput = new Thread() {
 			@Override
 			public void run() {
 				try {
 					runOutputThread();
 				} catch (final Throwable t) { // CS_IGNORE
 					Log.error(t, "Output-Thread died");
 				}
 			}
 		};
 		thrInput.start();
 		thrOutput.start();
 
 		Log.detail("Started waiting for threads");
 		while (thrOutput.isAlive())
 			Thread.sleep(100);
 		Log.detail("Output Thread no longer alive");
 		if (thrInput.isAlive()) {
 			Log.error("Input-Thread still alive but Output-Thread died");
 			thrInput.interrupt();
 			while (thrInput.isAlive())
 				Thread.sleep(100);
 		}
 		Log.detail("Input Thread no longer alive");
 		thrInput = null;
 		thrOutput = null;
 
 		// TODO call close() here directly?
 	}
 
 	protected void runInputThread() throws StateException, IOException {
 		int count = 0;
 		try {
 			Log.info("Input-Thread started");
			while (true) {
 				ensureInitialized();
 
 				// read next data block ...
 				final Data data = getFromInput();
 				if (data == null) break; // marks end of all inputs
 				++count;
 
 				// ... convert it ...
 				deadlockDetection.clear();
 				final List<Data> dataList = convertDataToDataList(data);
 
 				// ... and put it into the queue for Output classes
 				if (dataQueue.put(dataList)) {
 					Log.info("Canceling current command, "
 							+ "because of higher-priority command '%s'", data);
 					thrOutput.interrupt();
 				}
 			}
 		} finally {
 			Log.info("Input-Thread finished");
 			Log.detail("Read %d data blocks from input", count);
 			dataQueue.close();
 		}
 	}
 
 	protected void runOutputThread() throws StateException {
 		Log.info("Output-Thread started");
 		int count = 0;
 		try {
 			while (true) {
 				ensureInitialized();
 
 				// fetch next data block ...
 				final Data data;
 				try {
 					data = dataQueue.get();
 				} catch (final InterruptedException e1) {
 					Log.detail("Reading next data has been interrupted");
 					continue;
 				}
 				if (data == null) break; // Input-Thread has finished piping
 				++count;
 
 				// ... and send it to all currently registered outputs
 				try {
 					writeDataToAllOutputs(data);
 				} catch (final InterruptedException e) {
 					Log.detail("Outputting data '%s' has been interrupted",
 							data);
 				}
 			}
 		} finally {
 			Log.info("Output-Thread finished");
 			Log.detail("Sent %d data blocks to output", count);
 		}
 	}
 
 	private Data getFromInput() {
 		Log.detail("Reading one data block from input");
 		Input in;
 		while (true) {
 			assert inputPosition <= inputList.size();
 			if (inputPosition >= inputList.size()) {
 				Log.detail("InputList is empty");
 				return null;
 			}
 			in = inputList.get(inputPosition);
 			try {
 				if (in.canReadData()) {
 					final Data res = in.readData();
 					if (res == null)
 						throw new InputException(in, false,
 								"readData() returned null");
 					return res;
 				}
 			} catch (final InputException e) {
 				Log.error(e);
 				if (e.isPermanent()) {
 					Log.info("Skipping no longer working input '%s'", in);
 					in.tryClose();
 					++inputPosition;
 				} else
 					Log.info("Skipping one data block from input '%s'", in);
 				// try next data packet / try from next input
 				continue;
 			}
 			// no more data available in this Input, so
 			// switch to the next one
 			Log.info("Switching to next input");
 			in.tryClose();
 			++inputPosition;
 			// try data packet from next input
 		}
 	}
 
 	/**
 	 * Converts one {@link Data} object to a list of {@link Data} objects if a
 	 * fitting {@link Converter} could be found. Otherwise the {@link Data}
 	 * object itself is returned.
 	 * 
 	 * @param data
 	 *            The {@link Data} object to be converted.
 	 * @return A single-element list holding the data object given by
 	 *         <b>data</b> if no fitting {@link Converter} could be found or a
 	 *         list of new {@link Data} objects returned by the first fitting
 	 *         {@link Converter}.
 	 */
 	private List<Data> convertDataToDataList(final Data data) {
 		Log.detail("Converting data block to list of data blocks");
 		List<Data> res = null;
 		for (final Converter cvt : converterList)
 			if (!ignoredConverter.contains(cvt)
 					&& (res = convertOneData(data, cvt)) != null) return res;
 
 		// no fitting (and not ignored and working) converter found
 		Log.info("No Converter found, returning data as is: '%s'", data);
 		res = new ArrayList<Data>(1);
 		res.add(data);
 		return res;
 	}
 
 	private List<Data> convertOneData(final Data data, final Converter cvt) {
 		try {
 			if (cvt.canHandleData(data)) {
 				Log.detail("Converting '%s' with '%s'", data, cvt);
 				final long id = (long) cvt.hashCode() << 32 | data.hashCode();
 				if (!deadlockDetection.add(id))
 					throw new ConverterException(cvt, true,
 							"Detected dead-lock");
 				final List<Data> newDatas = cvt.convert(data);
 				final List<Data> res = new ArrayList<Data>(newDatas.size());
 				for (final Data newData : newDatas)
 					res.addAll(convertDataToDataList(newData));
 				return res;
 			}
 		} catch (final ConverterException e) {
 			Log.error(e);
 			if (e.isPermanent()) {
 				Log.info("Removing no longer working converter '%s'", cvt);
 				cvt.tryClose();
 				ignoredConverter.add(cvt);
 			} else
 				Log.info("Skipping converter '%s' for one data block '%s'",
 						cvt, data);
 		}
 		return null;
 	}
 
 	private void writeDataToAllOutputs(final Data data)
 			throws InterruptedException {
 		Log.detail("Writing data block to %d output(s)", outputList.size());
 		for (final Output out : outputList)
 			if (!ignoredOutputs.contains(out)) writeToOutput(data, out);
 	}
 
 	private void writeToOutput(final Data data, final Output out)
 			throws InterruptedException {
 		try {
 			out.write(data);
 		} catch (final OutputException e) {
 			Log.error(e);
 			if (e.isPermanent()) {
 				Log.info("Removing no longer working output '%s'", out);
 				out.tryClose();
 				ignoredOutputs.add(out);
 			} else
 				Log.info("Skipping output '%s' for one data block '%s'", out,
 						data);
 		}
 	}
 
 	public void reset() throws PipeException {
 		ensureNoLongerInitialized();
 		inputList.clear();
 		converterList.clear();
 		outputList.clear();
 		assert inputPosition == 0;
 		assert ignoredConverter.isEmpty();
 		assert ignoredOutputs.isEmpty();
 	}
 
 	public void writeToFile(final File file) throws IOException, PipeException {
 		ensureNoLongerInitialized();
 		final Writer out = new FileWriter(file);
 		for (final PipePart pp : inputList) {
 			out.write(pp.getClass().getSimpleName());
 			out.write(":\n");
 			pp.getConfig().writeToFile(out);
 		}
 		for (final PipePart pp : converterList) {
 			out.write(pp.getClass().getSimpleName());
 			out.write(":\n");
 			pp.getConfig().writeToFile(out);
 		}
 		for (final PipePart pp : outputList) {
 			out.write(pp.getClass().getSimpleName());
 			out.write(":\n");
 			pp.getConfig().writeToFile(out);
 		}
 		out.close();
 	}
 
 	public boolean readFromFile(final File file) throws IOException,
 			PipeException {
 		ensureNoLongerInitialized();
 		boolean skipped = false;
 		reset();
 		final BufferedReader in = new BufferedReader(new FileReader(file));
 		while (true) {
 			String line = in.readLine();
 			if (line == null) break;
 			line = line.trim();
 			if (!line.endsWith(":"))
 				throw new IOException("Missing ':' at end of line");
 			final String cn = line.substring(0, line.length() - 1);
 			final String pckName = getClass().getPackage().getName();
 			String fcn;
 			PipePart pp;
 			try {
 				try {
 					fcn = String.format("%s.in.%s", pckName, cn);
 					pp = (PipePart) getClass().getClassLoader().loadClass(fcn)
 							.newInstance();
 				} catch (final ClassNotFoundException e1) {
 					try {
 						fcn = String.format("%s.cvt.%s", pckName, cn);
 						pp = (PipePart) getClass().getClassLoader().loadClass(
 								fcn).newInstance();
 					} catch (final ClassNotFoundException e2) {
 						try {
 							fcn = String.format("%s.out.%s", pckName, cn);
 							pp = (PipePart) getClass().getClassLoader()
 									.loadClass(fcn).newInstance();
 						} catch (final ClassNotFoundException e3) {
 							throw new PipeException(null, false,
 									"Cannot find PipePart with class-name '%s' in any "
 											+ "package under '%s'", cn, pckName);
 						}
 					}
 				}
 			} catch (final InstantiationException e) {
 				throw new PipeException(null, false, e,
 						"Cannot create PipePart of class '%s'", cn);
 			} catch (final IllegalAccessException e) {
 				throw new PipeException(null, false, e,
 						"Cannot create PipePart of class '%s'", cn);
 			}
 			try {
 				in.mark(0);
 				pp.getConfig().readFromFile(in);
 			} catch (final IOException e) {
 				// skip this pipe part and try to read the next one
 				Log.error("Skipped reading '%s' from file:", cn);
 				Log.error(e);
 				in.reset();
 				skipped = true;
 				continue;
 			}
 			if (pp instanceof Input)
 				inputList.add((Input) pp);
 			else if (pp instanceof Converter)
 				converterList.add((Converter) pp);
 			else if (pp instanceof Output)
 				outputList.add((Output) pp);
 			else
 				throw new PipeException(pp, true,
 						"Cannot create PipePart of class '%s': "
 								+ "Unknown super class", cn);
 		}
 		in.close();
 		return !skipped;
 	}
 }
