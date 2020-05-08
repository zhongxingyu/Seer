 package br.usp.ime.tcc.activities;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Toast;
 import br.usp.ime.tcc.activities.components.ComponentUtils;
 import br.usp.ime.tcc.filter.BitmapFilter;
 import br.usp.ime.tcc.filter.colorhighlight.ColorHighlightBitmapFilter;
 import br.usp.ime.tcc.filter.simulation.SimulationBitmapFilter;
 import br.usp.ime.tcc.filter.visocor.VisocorBitmapFilter;
 import br.usp.ime.tcc.utils.Constants;
 import br.usp.ime.tcc.utils.FileSaver;
 import br.usp.ime.tcc.utils.Utils;
 
 public class BitmapFilterActivity extends Activity {
 	private ImageView filteredImage;
 	private Bitmap originalBitmap;
 	private Bitmap filteredBitmap;
 	private BitmapFilter filter;
 	private int filterType;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.bimap_filter);
 
 		getExtras();
 		loadComponents();
 	}
 
 	@Override
 	public void onBackPressed() {
 		goBackToFilterActivity();
 	}
 
 	private void getExtras() {
 		Bundle extras = getIntent().getExtras();
 
 		if (extras != null) {
 			String imagePath = (String) extras.get(Constants.IMAGE_PATH);
 			int imageOrientation = (Integer) extras
 					.get(Constants.IMAGE_ORIENTATION);
 			filterType = (Integer) extras.get(Constants.FILTER_TYPE);
 			originalBitmap = Utils.getScaledBitmapFromImagePath(imagePath,
 					imageOrientation);
 			loadFilter(filterType);
 			filteredBitmap = filter.applyTo(originalBitmap);
 		}
 	}
 
 	private void loadFilter(int filterType) {
 		switch (filterType) {
 		case Constants.VISOCOR_FILTER:
 			filter = new VisocorBitmapFilter(Constants.PROGRESS);
 			break;
 		case Constants.COLOR_HIGHLIGHT_FILTER:
			filter = new ColorHighlightBitmapFilter(255, 0, 0,
					Constants.DEFAULT_TOLERANCE, Constants.DEFAULT_TOLERANCE,
					Constants.DEFAULT_TOLERANCE);
 			break;
 		default:
 			filter = new SimulationBitmapFilter(filterType);
 			break;
 		}
 	}
 
 	private void loadComponents() {
 		final ComponentUtils componentUtils = new ComponentUtils(this);
 
 		filteredImage = componentUtils.loadImageView(R.id.filtered_image);
 
 		componentUtils.loadButton(R.id.save_button, new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				FileSaver fileSaver = new FileSaver();
 
 				try {
 					File file = fileSaver.saveToFile(filteredBitmap);
 					addToGallery(file);
 					Toast.makeText(BitmapFilterActivity.this,
 							getString(R.string.file_saved), Toast.LENGTH_SHORT)
 							.show();
 					goBackToFilterActivity();
 				} catch (IOException e) {
 					Toast.makeText(BitmapFilterActivity.this,
 							getString(R.string.try_again), Toast.LENGTH_LONG)
 							.show();
 				}
 
 			}
 
 			private void addToGallery(File file) {
 				Intent mediaScanIntent = new Intent(
 						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 				Uri contentUri = Uri.fromFile(file);
 				mediaScanIntent.setData(contentUri);
 				BitmapFilterActivity.this.sendBroadcast(mediaScanIntent);
 			}
 		});
 
 		componentUtils.loadButton(R.id.discard_button, new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				goBackToFilterActivity();
 			}
 		});
 
 		componentUtils.fillIn(filteredImage, filteredBitmap);
 
 		if (filterType == Constants.VISOCOR_FILTER) {
 			componentUtils.loadSeekBar(R.id.intensity_bar,
 					Constants.MAX_INTENSITY, Constants.PROGRESS,
 					new OnSeekBarChangeListener() {
 						@Override
 						public void onStopTrackingTouch(SeekBar arg0) {
 						}
 
 						@Override
 						public void onStartTrackingTouch(SeekBar arg0) {
 						}
 
 						@Override
 						public void onProgressChanged(SeekBar seekBar,
 								int progress, boolean fromUser) {
 							filter.update(progress);
 							filteredBitmap = filter.applyTo(originalBitmap);
 
 							componentUtils
 									.fillIn(filteredImage, filteredBitmap);
 						}
 					});
 		} else {
 			componentUtils.hideSeekBar(R.id.intensity_bar);
 		}
 	}
 
 	private void goBackToFilterActivity() {
 		finish();
 	}
 }
