 package net.fikovnik.projects.taco.core.internal.graphwiz;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import net.fikovnik.projects.taco.core.graphwiz.GraphwizException;
 import net.fikovnik.projects.taco.core.graphwiz.IGraphwiz;
 
 import org.apache.commons.io.IOUtils;
 
 public final class Graphwiz implements IGraphwiz {
 
 	private static class StreamGobbler extends Thread {
 		private final InputStream is;
 		private final StringWriter sw = new StringWriter();
 		private AtomicBoolean stop = new AtomicBoolean(false);
 
 		public StreamGobbler(InputStream is) {
 			this.is = is;
 		}
 
 		public void shutdown() {
 			this.stop.set(true);
 		}
 		
 		public void run() {
 			try {
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				String line = null;
 				while ((line = br.readLine()) != null && !stop.get()) {
 					sw.write(line+"\n");
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			} finally {
 				IOUtils.closeQuietly(is);
 				IOUtils.closeQuietly(sw);
 			}
 		}
 		
 		@Override
 		public String toString() {
 			return sw.toString();
 		}
 	}
 
 	private final String dotPath;
 
 	public Graphwiz(String dotPath) {
 		this.dotPath = dotPath;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.fikovnik.projects.taco.core.internal.graphwiz.IGraphwitz#generatePDF
 	 * (java.io.File, java.io.File)
 	 */
 	@Override
 	public void generate(File in, File out, GraphwizOutputType type) throws GraphwizException {
 		Runtime r = Runtime.getRuntime();
 		StreamGobbler stderr = null;
 		StreamGobbler stdout = null;
 		
 		try {
			System.out.println(in.getAbsolutePath());
 			Process p = r.exec(String.format("%s -T %s %s -o %s", dotPath, type.getFileExtension(),
 					in.getAbsolutePath(), out.getAbsolutePath()));
 			
 			stderr = new StreamGobbler(p.getErrorStream());
 			stderr.start();
 			stdout = new StreamGobbler(p.getInputStream());
 			stdout.start();
 			
 			int exitCode = p.waitFor();
 			
 			if (exitCode != 0) {
 				throw new GraphwizException("Unable to generate PDF from " + in
 						+ ". Dot process exited with " + exitCode+"\nOutput: "+stdout.toString()+"\n"+stderr.toString());
 			}
 		} catch (IOException e) {
 			throw new GraphwizException("Unable to generate PDF from " + in
 					+ ".", e);
 		} catch (InterruptedException e) {
 			throw new GraphwizException("Unable to generate PDF from " + in
 					+ ". Process interrupted", e);
 		} finally {
 			if (stderr != null) {
 				stderr.shutdown();
 			}
 			if (stdout != null) {
 				stdout.shutdown();
 			}
 		}
 	}
 
 }
