 package com.cateye.core.native_;
 
 import com.cateye.core.IPreciseBitmap;
 import com.cateye.core.IProgressListener;
 import com.cateye.core.stage.IStageOperationProcessor;
 import com.cateye.stageoperations.brightness.BrightnessStageOperation;
 
 class BrightnessStageOperationProcessor implements IStageOperationProcessor<BrightnessStageOperation>
 {
 	static native void Process(PreciseBitmap bmp, BrightnessStageOperation operation);
 	
 	static
 	{
		LibraryLoader.attach("brightness.CatEyeOperation");
 	}
 
 	@Override
 	public int calculateEffort() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void process(BrightnessStageOperation params, IPreciseBitmap bitmap, IProgressListener progressListener) {
 		Process((PreciseBitmap)bitmap, params);
 	}
 }
