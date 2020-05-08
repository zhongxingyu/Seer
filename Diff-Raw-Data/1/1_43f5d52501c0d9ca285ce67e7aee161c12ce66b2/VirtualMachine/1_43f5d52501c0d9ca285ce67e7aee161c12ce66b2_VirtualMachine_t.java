 package com.mauricecodik.icfp2009;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import clojure.lang.AFn;
 import clojure.lang.IFn;
 import clojure.lang.IPersistentMap;
 
 public class VirtualMachine {
 	
 	public static void main(String[] args) throws Exception {
 		String filename = args[0];
 		int configuration = Integer.parseInt(args[1]);
 		
 		final Visualizer vz = new Visualizer(500, 1.0/100000.0);
 		final VirtualMachine vm = new VirtualMachine();
 
 		AFn callback = new AFn() {
 			@Override
 			public Object invoke(Object arg1) throws Exception {
 				double target = vm.getOutput(4);
 				double mex = vm.getOutput(2);
 				double mey = vm.getOutput(3);
 
 				vz.addCircle(0, 0, target);
 				vz.addCircle(0, 0, 6357000.0);
 				
 				if (vm.getCurrentIteration() % 50 == 0) {
 					vz.addPoint("me", mex, mey);
 					vz.repaint();
 				}
 				
 				return null;
 			}
 		};
 		
 		vm.load(filename);
 		vm.run(configuration, 10000, callback, true);
 	}
 	
 	public VirtualMachine() {
 		input = new double[16385];
 		output = new double[16385];
 		data = new double[16385];
 		program = new long[16385];
 	}
 
 	private double[] data; 
 	private long[] program; 
 	
 	private double[] input;
 	private double[] output;
 	
 	private int iteration;
 
 	boolean status = false;
 	
 	private List<InputAction> trace;
 	private int configuration;
 	
 	private class InputAction {
 		int t;
 		int addr;
 		double value;
 	}
 	
 	public int getCurrentIteration() {
 		return iteration;
 	}
 	
 	public void setInput(int addr, double value) {
 		if (trace == null) {
 			trace = new ArrayList<InputAction>();
 		}
 		
 		input[addr] = value;
 		
 		InputAction ia = new InputAction();
 		ia.t = iteration;
 		ia.addr = addr;
 		ia.value = value;
 		
 		trace.add(ia);
 	}
 	
 	public void emitTrace(String filename) throws IOException {
 		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
 		try {
 			byte[] buf = new byte[4096];
 			ByteBuffer traceBuffer = ByteBuffer.wrap(buf);
 			traceBuffer.order(ByteOrder.LITTLE_ENDIAN);
 			traceBuffer.putInt(0xCAFEBABE);
 			traceBuffer.putInt(668);
 			traceBuffer.putInt(configuration);
 			
 			out.write(buf, 0, traceBuffer.position());
 			
 			traceBuffer.clear();
 			
 			List<InputAction> currentFrame = null; 
 			for (int i = 0; i < trace.size(); i += currentFrame.size()) {
 				int time = trace.get(i).t;
 				traceBuffer.putInt(time);
 				currentFrame = new ArrayList<InputAction>();
 				currentFrame.add(trace.get(i));
 				
 				for (int k = i+1; k < trace.size(); k++) {
 					InputAction ia = trace.get(k);
 					if (ia.t == time) {
 						currentFrame.add(ia);
 					}
 				}
 				
 				traceBuffer.putInt(currentFrame.size());
 				for (InputAction cfia : currentFrame) {
 					traceBuffer.putInt(cfia.addr);
 					traceBuffer.putDouble(cfia.value);
 				}
 				
 				out.write(buf, 0, traceBuffer.position());
 				traceBuffer.clear();
 			}
 				
 			traceBuffer.putInt(iteration+1);
 			traceBuffer.putInt(0);
 			out.write(buf, 0, traceBuffer.position());
 			traceBuffer.clear();
 
 			out.flush();
 		}
 		finally {
 			out.close();
 		}
 	}
 	
 	public double getOutput(int addr) {
 		return output[addr];
 	}
 	
 	private int programSize;
 	
 	public void load(String filename) throws Exception {
 
 		Arrays.fill(data, 0.0);
 		Arrays.fill(program, 0l);
 		
 		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
 		
 		byte[] buf = new byte[12];
 		int frame = 0;
 		try {
 			while (inputStream.read(buf, 0, 12) > 0) {
 				
 				if (frame > data.length) {
 					throw new Exception("reading more than " + data.length + " frames?");
 				}
 				
 				int instruction;
 				double datum;
 				
 				ByteBuffer bb = ByteBuffer.wrap(buf);
 				bb.order(ByteOrder.LITTLE_ENDIAN);
 				
 				if (frame % 2 == 0) {
 					datum = bb.getDouble();
 
 //					//System.out.println("read double: " + datum);
 					instruction = bb.getInt();
 
 					////System.out.println("read int: " + Integer.toHexString(instruction));
 				}
 				else {
 					instruction = bb.getInt();
 					////System.out.println("read int: " + Integer.toHexString(instruction));
 
 					datum = bb.getDouble();
 
 					////System.out.println("read double: " + datum);
 				}
 				
 				data[frame] = datum;
 				program[frame] = instruction;
 				
 				frame++;
 				
 				Arrays.fill(buf, (byte)0);
 			}
 		} 
 		finally {
 			inputStream.close();
 		}
 		
 		programSize = frame;
 		
 		//System.out.println("loaded " + frame + " frames from " + filename);
 	}
 	
 	public void run(int configuration, int maxIterations, IFn callback, boolean visualize) {
 
 		this.configuration = configuration;
 		
 		Arrays.fill(input, 0.0);
 		Arrays.fill(output, 0.0);
 
 		setInput(0x3e80, configuration);
 
 		Visualizer vz = null;
 		if (visualize) { 
 			if (configuration > 1000 && configuration < 2000) {
 				vz = new Visualizer(900, 1.0/100000.0);
 			}
 			else if (configuration == 2001){
 				vz = new Visualizer(900, 1.0/50000.0);
 			}
 			else {
 				vz = new Visualizer(900, 1.0/500000.0);
 			}
 		}
 	
 		long startTime = System.currentTimeMillis();
 		
 		for (iteration = 0; iteration < maxIterations; iteration++) {
 			
 			runIteration();
 			
 			double mex = getOutput(2);
 			double mey = getOutput(3);
 
 			if (vz != null) {
 				if (configuration > 1000 && configuration < 2000) {
 					double target = getOutput(4);
 					vz.addCircle(0, 0, target);
 				}
 				else if (configuration > 2000) {
 					double themx = getOutput(4);
 					double themy = getOutput(5);
 	
 					if (iteration % 50 == 0) {
 						vz.addPoint("them", themx - mex, themy - mey);
 					}
 				}
 				
 				vz.addCircle(0, 0, 6357000.0);
 				
 				if (iteration % 50 == 0)
 					vz.addPoint("me", mex, mey);
 				
 				vz.repaint();
 			}
 			
 			if (getOutput(0) != 0.0) {
 				System.err.println("Configuration " + configuration + " done after " + iteration + " iterations, score: " + getOutput(0));
 				try {
 					emitTrace("traces/" + configuration + "-" + getOutput(0) + ".trace");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 				try {
 					Thread.sleep(3000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return;
 			}
 			else {
 				try {
 					if (callback != null) {
 						callback.invoke(this);
 					}
 				}
 				catch (Exception e) {
 					throw new RuntimeException("caught exn from callback", e);
 				}
 
 			}
 			
 			if (iteration % 10000 == 0) {
 				long now = System.currentTimeMillis();
 				
 				double secondsElapsed = (now-startTime)/1000.0;
 				
 				System.err.println("iteration " + iteration + ": " + (iteration/secondsElapsed) + " iter/s");
 			}
 		}
 		
 		System.err.println("Aborting configuration " + configuration + " after max " + iteration + " iterations");
 	}
 	
 	
 	
 	public int opcode(long instruction) {
 		return (int)((instruction >> 28) & 0xF);
 	}
 	
 	public void runIteration() {
 		for (int pc = 0; pc < programSize; pc++) {
 			long instruction = program[pc];
 
 			int opcode = opcode(instruction);
 			if (opcode == 0) {
 				
 				int immediate = (int)((instruction >> 14)&1023);
 				int addr = (int)(instruction & 16383);
 				int sopcode = (int)((instruction >> 24)&15); 
 				
 				runSOp(pc, sopcode, immediate, addr);
 			}
 			else {
 				
 				int addr2 = (int)(instruction & 16383);
 				int addr1 = (int)((instruction >> 14) & 16383);
 				
 				runDOp(pc, opcode, addr1, addr2);
 			}
 		}
 	}
 	
 	private void runSOp(int pc, int opcode, int immediate, int addr) {
 		try {
 			if (opcode == 0) {
 				//System.out.println(pc+ ": noop");
 				return; 
 			}
 			
 			if (opcode == 1) { // CMP
 				int comparator = (int)((immediate>>7)&7);
 				if (comparator == 0) {
 					status = (data[addr] < 0);
 					//System.out.println(pc+ ": cmpz LTZ, r" + addr);
 				}
 				else if (comparator == 1) {
 					status = (data[addr] <= 0);
 					//System.out.println(pc+ ": cmpz LTE, r" + addr);
 				}
 				else if (comparator == 2) {
 					status = (data[addr] == 0);
 					//System.out.println(pc+ ": cmpz EQZ, r" + addr);
 				}
 				else if (comparator == 3) {
 					status = (data[addr] >= 0);
 					//System.out.println(pc+ ": cmpz GTE, r" + addr);
 				}
 				else if (comparator == 4) {
 					status = (data[addr] > 0);
 					//System.out.println(pc+ ": cmpz GTZ, r" + addr);
 				}
 				else {
 					throw new RuntimeException("Bad CMP instruction: opcode = " + opcode + "; imm = " + immediate + "; addr = " + addr);
 				}
 				return;
 			}
 			
 			if (opcode == 2) {
 				data[pc] = Math.sqrt( data[addr] );
 				//System.out.println(pc+ ": sqrt r" + addr);
 				return;
 			}
 			
 			if (opcode == 3) {
 				data[pc] = data[addr];
 				//System.out.println(pc+ ": copy r" + addr);
 				return;
 			}
 			
 			if (opcode == 4) {
 				data[pc] = input[addr];
 				//System.out.println(pc+ ": input r" + addr);
 				return;
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			throw new RuntimeException("Out of bounds while running: opcode = " + opcode + "; imm = " + immediate + "; addr = " + addr, e);
 		}
 		throw new RuntimeException("Bad S op: opcode = " + opcode + "; imm = " + immediate + "; addr = " + addr);
 	}
 	
 	private void runDOp(int pc, int opcode, int addr1, int addr2) {
 		try { 
 			if (opcode == 1) {
 				data[pc] = data[addr1] + data[addr2];
 				//System.out.println(pc+ ": add r" + addr1 + " r" + addr2);
 				return;
 			}
 			
 			if (opcode == 2) {
 				data[pc] = data[addr1] - data[addr2];
 				//System.out.println(pc+ ": sub r" + addr1 + " r" + addr2);
 				return;
 			}
 			
 			if (opcode == 3) {
 				data[pc] = data[addr1] * data[addr2];
 				//System.out.println(pc+ ": mult r" + addr1 + " r" + addr2);
 				return;
 			}
 			
 			if (opcode == 4) {
 				
 				if (data[addr2] == 0.0) {
 					data[pc] = 0.0;
 				}
 				else {
 					data[pc] = data[addr1] / data[addr2];
 				}
 				
 				//System.out.println(pc+ ": div r" + addr1 + " r" + addr2);
 				return;
 			}
 			
 			if (opcode == 5) {
 				output[addr1] = data[addr2];
 				//System.out.println(pc+ ": output r" + addr1 + " r" + addr2);
 				return;
 			}
 			
 			if (opcode == 6) {
 				
 				int src = status ? addr1 : addr2;
 				data[pc] = data[src];
 	
 				//System.out.println(pc+ ": phi r" + addr1 + " r" + addr2);
 				return;			
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			throw new RuntimeException("Out of bounds while running: opcode = " + opcode + "; addr1 = " + addr1 + "; addr2 = " + addr2, e);
 		}
 		
 		throw new RuntimeException("Bad D instruction: opcode = " + opcode + "; addr1 = " + addr1 + "; addr2 = " + addr2);
 	}
 }
