 package it.polito.atlas.alea2.adapters;
 
 import static it.polito.atlas.alea2.components.DisplaySingleton.display;
 import static it.polito.atlas.alea2.components.ShellSingleton.shell;
 import static it.polito.atlas.alea2.components.StatusBarSingleton.status;
 import static org.eclipse.swt.SWT.OPEN;
 
 import java.io.File;
//import java.util.concurrent.TimeUnit;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
//import org.gstreamer.Gst;
//import org.gstreamer.Pipeline;
 import org.gstreamer.State;
 import org.gstreamer.elements.PlayBin2;
 import org.gstreamer.swt.VideoComponent;
 
 public class OpenAdapter extends SelectionAdapter {
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		FileDialog dialog = new FileDialog(shell(), OPEN);
 
 		String[] filterNames = new String[] { "All Files (*)", "Java sources" };
 
 		String[] filterExtensions = new String[] { "*", "*.java" };
 
 		dialog.setFilterNames(filterNames);
 		dialog.setFilterExtensions(filterExtensions);
 
 		String path = dialog.open();
 		if (path != null) {
 			status().setText(path);
 			status().pack();
 		}
 		// play(path);
 		playSWT(path);
 	}
 
 	/*
 	 * public static void play(String file) { String args[] = {"",""}; args =
 	 * Gst.init("Alea2", args); final PlayBin playbin = new
 	 * PlayBin("VideoPlayer"); try { playbin.setInputFile(new File(file)); }
 	 * catch (Exception e) { playbin.setInputFile(new
 	 * File("E:\\aMule\\Incoming\\D&D - The Gamers - sub ITA.avi")); }
 	 * 
 	 * SwingUtilities.invokeLater(new Runnable() {
 	 * 
 	 * public void run() { VideoComponent videoComponent = new VideoComponent();
 	 * playbin.setVideoSink(videoComponent.getElement());
 	 * 
 	 * JFrame frame = new JFrame("VideoPlayer");
 	 * frame.getContentPane().add(videoComponent, BorderLayout.CENTER);
 	 * frame.setPreferredSize(new Dimension(640, 480));
 	 * frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 	 * frame.pack(); frame.setVisible(true); playbin.setState(State.PLAYING); }
 	 * }); Gst.main(); playbin.setState(State.NULL); }
 	 */
 	public void playSWT(String file) {
//		Pipeline videos = new Pipeline();
 
 		final PlayBin2 playbin = new PlayBin2("VideoPlayer");
 
/*		final PlayBin2 playbin2 = new PlayBin2("VideoPlayer");
 
 		videos.addMany(playbin, playbin2);
 		videos.seek(10, TimeUnit.SECONDS);
*/
 		try {
 			playbin.setInputFile(new File(file));
//			playbin2.setInputFile(new File(file));
 		} catch (Exception e) {
 			playbin.setInputFile(new File("D:\\a.avi"));
//			playbin2.setInputFile(new File("D:\\a.avi"));
 		}
 
 		try {
 			Shell shell2 = new Shell(display());
 			shell2.addShellListener(new ShellListener() {
 
 				@Override
 				public void shellIconified(ShellEvent e) {
 					playbin.pause();
 				}
 
 				@Override
 				public void shellDeiconified(ShellEvent e) {
 					playbin.play();
 				}
 
 				@Override
 				public void shellDeactivated(ShellEvent e) {
 				}
 
 				@Override
 				public void shellClosed(ShellEvent e) {
 					playbin.stop();
 					playbin.dispose();
 				}
 
 				@Override
 				public void shellActivated(ShellEvent e) {
 				}
 			});
 			shell2.setSize(640, 480);
 			shell2.setLayout(new FillLayout());
 			shell2.setText("SWT Video Test - " + file);
 
 			final VideoComponent component = new VideoComponent(shell2, SWT.NONE);
 			component.getElement().setName("video");
 			component.setKeepAspect(true);
 			// shell2.setSize(component.getSize());
 			// component.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 			// true));//GridData.FILL_BOTH));
 
 			playbin.setVideoSink(component.getElement());
 			// shell2.pack();
 			shell2.open();
 
//			videos.setState(State.PLAYING);
			playbin.setState(State.PLAYING);
 
 			// Element sink = component.getElement();
 
 			// pipe.addMany(src, id, sink);
 			// Element.linkMany(src, id, sink);
 			// pipe.setState(State.PLAYING);
 
 			// while (!shell2.isDisposed()) {
 			// if (!display.readAndDispatch())
 			// display.sleep();
 			// }
 			// display.dispose();
 
 		} catch (Exception e) {
 		}
 	}
 
 }
