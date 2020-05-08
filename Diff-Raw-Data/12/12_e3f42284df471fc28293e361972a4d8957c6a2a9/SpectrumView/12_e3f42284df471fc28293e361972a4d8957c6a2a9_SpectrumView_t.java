 package edu.tamu.csce470.mir;
 
 import java.util.ArrayList;
 
 import edu.tamu.csce470.mir.Spectrum.DisplayMode;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public class SpectrumView extends View
 {
 	Spectrum spectrum;
 	CalibrationSettings calibration;
 	
 	Bitmap baselineImage;
 	Bitmap sampleImage;
 	
 	Paint borderPaint;
 	Paint baselinePaint;
 	Paint samplePaint;
 	Paint absorbancePaint;
 	Paint sampleRowPaint;
 	Paint boundaryPaint;
 	
 	public SpectrumView(Context context, AttributeSet attributes)
 	{
 		super(context, attributes);
 		
 		spectrum = null;
 		calibration = null;
 		
 		baselineImage = null;
 		sampleImage = null;
 		
 		borderPaint = new Paint();
 		borderPaint.setColor(Color.BLACK);
 		borderPaint.setStyle(Style.STROKE);
 		
 		baselinePaint = new Paint();
 		baselinePaint.setColor(Color.rgb(0, 0, 200));
 		baselinePaint.setStrokeWidth(4.0f);
 		
 		samplePaint = new Paint();
 		samplePaint.setColor(Color.rgb(200, 0, 0));
 		samplePaint.setStrokeWidth(4.0f);
 		
 		absorbancePaint = new Paint();
 		absorbancePaint.setColor(Color.rgb(0, 200, 0));
 		absorbancePaint.setStrokeWidth(4.0f);
 		
 		sampleRowPaint = new Paint();
 		sampleRowPaint.setColor(Color.RED);
 		
 		boundaryPaint = new Paint();
 		boundaryPaint.setColor(Color.CYAN);
 	}
 	
 	public void setSpectrum(Spectrum spectrum)
 	{
 		this.spectrum = spectrum;
 		
 		if (spectrum.getBaselineUri() == null || spectrum.getDisplayMode() != Spectrum.DisplayMode.BASELINE_IMAGE)
 		{
 			baselineImage = null;
 		}
 		else
 		{
 			try
 			{
 				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
 				baselineImage = BitmapFactory.decodeFile(spectrum.getBaselineUri().getPath(), options);
 			}
 			catch (Exception e)
 			{
 				Log.e("SpectrumView", "Failed to load the baseline image from " + spectrum.getBaselineUri().getPath());
 			}
 		}
 		
 		if (spectrum.getSampleUri() == null || spectrum.getDisplayMode() != Spectrum.DisplayMode.SAMPLE_IMAGE)
 		{
 			sampleImage = null;
 		}
 		else
 		{
 			try
 			{
 				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
 				sampleImage = BitmapFactory.decodeFile(spectrum.getSampleUri().getPath(), options);
 			}
 			catch (Exception e)
 			{
 				Log.e("SpectrumView", "Failed to load the sample image from " + spectrum.getSampleUri().getPath());
 			}
 		}
 	}
 	
 	public void releaseSpectrum()
 	{
 		if (baselineImage != null)
 		{
 			baselineImage.recycle();
 		}
 		
 		if (sampleImage != null)
 		{
 			sampleImage.recycle();
 		}
 		
 		spectrum = null;
 	}
 	
 	public void setCalibration(CalibrationSettings calibration)
 	{
 		this.calibration = calibration;
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh)
 	{
 		super.onSizeChanged(w, h, oldw, oldh);
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		
 		if (spectrum != null)
 		{
 			switch (spectrum.getDisplayMode())
 			{
 			case BASELINE_IMAGE:
 				drawBaselineBitmap(canvas);
 				break;
 			case SAMPLE_IMAGE:
 				drawSampleBitmap(canvas);
 				break;
 			case SPECTRUM_GRAPH:
 				drawSpectrumGraph(canvas);
 				break;		
 			}
 		}
 		
 		drawCalibrationLines(canvas);
 	}
 	
 	private void drawBaselineBitmap(Canvas canvas)
 	{
 		if (baselineImage != null)
 		{
 			Bitmap drawBitmap = Bitmap.createScaledBitmap(baselineImage, canvas.getWidth(), canvas.getHeight(), false);
 			canvas.drawBitmap(drawBitmap, 0, 0, null);
 		}
 	}
 	
 	private void drawSampleBitmap(Canvas canvas)
 	{
 		if (sampleImage != null)
 		{
 			Bitmap drawBitmap = Bitmap.createScaledBitmap(sampleImage, canvas.getWidth(), canvas.getHeight(), false);
 			canvas.drawBitmap(drawBitmap, 0, 0, null);
 		}
 	}
 	
 	private void drawSpectrumGraph(Canvas canvas)
 	{
 		canvas.drawRect(1, 1, canvas.getWidth(), canvas.getHeight(), borderPaint);
 
 		ArrayList<Integer> baselineIntensities = spectrum.getBaselineIntensities();
 		ArrayList<Integer> sampleIntensities = spectrum.getSampleIntensities();
 		ArrayList<Integer> absorbancies = spectrum.getAbsorbancies();	
 		
 		if (baselineIntensities != null)
 		{
 			float canvasWToSpectrumW = (float) canvas.getWidth() / (float) baselineIntensities.size();
 			float canvasHToSpectrumH = (float) canvas.getHeight() / (float) 255;
 			
 			for (int i = 0; i < canvas.getWidth(); i++)
 			{
 				int spectrumIndex = (int) (i / canvasWToSpectrumW);
 				
 				assert(spectrumIndex <= baselineIntensities.size());
 				if (spectrumIndex >= baselineIntensities.size())
 				{
 					spectrumIndex = baselineIntensities.size() - 1;
 				}
 				
 				int intensity = baselineIntensities.get(spectrumIndex);
 				int height = canvas.getHeight() - (int) (intensity * canvasHToSpectrumH);
 				
 				canvas.drawPoint(i, height, baselinePaint);
 			}
 		}
 		
 		if (sampleIntensities != null)
 		{
 			float canvasWToSpectrumW = (float) canvas.getWidth() / (float) sampleIntensities.size();
 			float canvasHToSpectrumH = (float) canvas.getHeight() / (float) 255;
 			
 			for (int i = 0; i < canvas.getWidth(); i++)
 			{
 				int spectrumIndex = (int) (i / canvasWToSpectrumW);
 				
 				assert(spectrumIndex <= sampleIntensities.size());
 				if (spectrumIndex >= sampleIntensities.size())
 				{
 					spectrumIndex = sampleIntensities.size() - 1;
 				}
 				
 				int intensity = sampleIntensities.get(spectrumIndex);
 				int height = canvas.getHeight() - (int) (intensity * canvasHToSpectrumH);
 				
 				canvas.drawPoint(i, height, samplePaint);
 			}
 		}
 		
 		if (absorbancies != null)
 		{
 			float canvasWToSpectrumW = (float) canvas.getWidth() / (float) absorbancies.size();
 			float canvasHToSpectrumH = (float) canvas.getHeight() / (float) 255;
 			
 			for (int i = 0; i < canvas.getWidth(); i++)
 			{
 				int spectrumIndex = (int) (i / canvasWToSpectrumW);
 				
 				assert(spectrumIndex <= absorbancies.size());
 				if (spectrumIndex >= absorbancies.size())
 				{
 					spectrumIndex = absorbancies.size() - 1;
 				}
 				
 				int intensity = absorbancies.get(spectrumIndex);
 				int height = canvas.getHeight() - (int) (intensity * canvasHToSpectrumH);
 				
 				canvas.drawPoint(i, height, absorbancePaint);
 			}
 		}
 	}
 	
 	private void drawCalibrationLines(Canvas canvas)
 	{
 		assert(calibration != null);
 		
 		int sampleRowY = Math.round(((float) calibration.sampleRow / (float) calibration.imageHeight) * canvas.getHeight());
 		int leftBoundaryX = Math.round(((float) calibration.startPixel / (float) calibration.imageWidth) * canvas.getWidth());
 		int rightBoundaryX = Math.round(((float) calibration.endPixel / (float) calibration.imageWidth) * canvas.getWidth());
 		
 		if (spectrum.getDisplayMode() != DisplayMode.SPECTRUM_GRAPH)
 		{
 			canvas.drawLine(0, sampleRowY, canvas.getWidth(), sampleRowY, sampleRowPaint);
 		}
 		
 		canvas.drawLine(leftBoundaryX, 0, leftBoundaryX, canvas.getHeight(), boundaryPaint);
 		canvas.drawLine(rightBoundaryX, 0, rightBoundaryX, canvas.getHeight(), boundaryPaint);
 	}
 }
