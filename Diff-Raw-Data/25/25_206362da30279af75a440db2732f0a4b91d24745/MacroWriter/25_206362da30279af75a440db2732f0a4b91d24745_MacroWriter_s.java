 package yang.events.macro;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import yang.events.eventtypes.YangEvent;
 import yang.model.DebugYang;
 import yang.surface.YangSurface;
 
 public class MacroWriter {
 
 	public static float FLUSH_INTERVAL = 0.6f;
 
 	protected DataOutputStream mWriter;
 	protected long startFrame;
 	protected YangSurface mSurface;
 	protected AbstractMacroIO mMacroIO;
 	protected long mEventCount;
 	protected double mLastFlush;
 
 	public MacroWriter(OutputStream writer,AbstractMacroIO macroIO) {
 		mWriter = new DataOutputStream(writer);
 		mSurface = macroIO.mSurface;
 		mMacroIO = macroIO;
 		start();
 	}
 
 	public void start() {
 		startFrame = mSurface.mStepCount;
 		mLastFlush = mSurface.mProgramTime;
 	}
 
 	public void writeEvent(YangEvent event) throws IOException {
 		mWriter.writeLong(mSurface.mStepCount-startFrame);
 		mMacroIO.writeEvent(mWriter,event);
 		if(mSurface.mProgramTime-mLastFlush>FLUSH_INTERVAL) {
 			mWriter.flush();
 			mLastFlush = mSurface.mProgramTime;
 		}
 		mEventCount++;
 	}
 
 	public void close() {
 		try {
 			mWriter.close();
 		} catch (IOException e) {
 			DebugYang.exception(e);
 		}
 	}
 }
