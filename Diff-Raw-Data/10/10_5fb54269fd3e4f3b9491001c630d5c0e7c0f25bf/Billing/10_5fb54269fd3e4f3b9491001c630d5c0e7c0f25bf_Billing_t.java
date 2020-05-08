 package fr.ravenfeld.library.billing;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import net.robotmedia.billing.BillingController;
 import net.robotmedia.billing.BillingRequest.ResponseCode;
 import net.robotmedia.billing.helper.AbstractBillingActivity;
 import net.robotmedia.billing.model.Transaction;
 import net.robotmedia.billing.model.Transaction.PurchaseState;
 
 import java.util.List;
 
 
 public class Billing extends AbstractBillingActivity {
 
 
 	private Button mBuyButton;
 	private Button mExitButton;
 	private TextView mTextView;
 	private SharedPreferences mSharedPreferences;
 	private String mIdProduct;
 	private String mKeyPublicBilling;
 	private String mKeyPrefBilling;
 	private double mSizePopup;
 	private double mSizePadding;
 	private int mIdImage;
 
 
 
     public void onCreate(Bundle savedInstanceState, String sharedPrefsName, String keyPrefBilling, String idProduct, String keyPublicBilling,
 			int idImage, double sizePopup, double sizePadding) {
 		super.onCreate(savedInstanceState);
 		mSharedPreferences = getBaseContext().getSharedPreferences(sharedPrefsName, 0);
 		mKeyPrefBilling = keyPrefBilling;
 		mIdProduct = idProduct;
 		mKeyPublicBilling = keyPublicBilling;
 		mSizePopup = sizePopup;
 		mSizePadding = sizePadding;
 		mIdImage = idImage;
 		BillingController.registerObserver(mBillingObserver);
 		BillingController.checkBillingSupported(this);
 	}
 
    protected void onPause(){
	super.onPause();
       	finish();
    }
    
 	private void showPopup() {
 		boolean appBuy = mSharedPreferences.getBoolean(mKeyPrefBilling, false);
 		if (!appBuy) {
 			setContentView(R.layout.main);
 			initView();
 			initButtons();
 		} else {
 			finish();
 		}
 	}
 
 	private void initView() {
 
 		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		int layoutWidth = (int) (display.getWidth() * mSizePopup);
 		if (layoutWidth % 2 == 0) {
 			layoutWidth += 1;
 		}
 		LinearLayout popup = (LinearLayout) getLayoutInflater().inflate(R.layout.popup, null);
 		LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(layoutWidth, 0);
 		layoutParam.height = LayoutParams.FILL_PARENT;
 
 		LinearLayout main = (LinearLayout) findViewById(R.id.main);
 
 		main.removeAllViewsInLayout();
 
 		main.addView(popup, layoutParam);
 
 		final float dpi = getResources().getDisplayMetrics().density;
 		int pixel = Math.round((float) ((mSizePadding * dpi)));
 
 		float sizePopup = (float) (display.getWidth() * mSizePopup);
 
 		if (sizePopup % 2 == 0) {
 			sizePopup += 1;
 		}
 
 		int imageWidth = Math.round(sizePopup - pixel); // to
 		if (imageWidth % 2 == 0) {
 			imageWidth += 1;
 		}
 
 		ImageView image = (ImageView) findViewById(R.id.image);
 		Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), mIdImage);
 		image.setImageBitmap(scaleBitmap(imageBitmap, layoutWidth));
 
 		mTextView = (TextView) findViewById(R.id.text);
 	}
 
 	private Bitmap scaleBitmap(Bitmap b, int width) {
 		Bitmap result = null;
 		int bw = b.getWidth();
 		int bh = b.getHeight();
 		double s = (double) width / (double) bw;
 		int newHeight = (int) (bh * s);
 
 		result = Bitmap.createScaledBitmap(b, width, newHeight, false);
 		return (result);
 	}
 
 	private void initButtons() {
 		mBuyButton = (Button) findViewById(R.id.unblock_button);
 		mBuyButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				BillingController.requestPurchase(Billing.this, mIdProduct, true);
 			}
 		});
 
 		mExitButton = (Button) findViewById(R.id.exit_button);
 		mExitButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 
 
 		boolean app_buy = mSharedPreferences.getBoolean(mKeyPrefBilling, false);
 		if (app_buy) {
 			mExitButton.setVisibility(Button.VISIBLE);
 			mBuyButton.setVisibility(Button.GONE);
 		} else {
 			mExitButton.setVisibility(Button.VISIBLE);
 			mBuyButton.setVisibility(Button.VISIBLE);
 		}
 
 	}
 
 	@Override
 	public byte[] getObfuscationSalt() {
 		return new byte[] { 41, -90, -116, -41, 66, -53, 122, -110, -127, -96, -88, 77, 127, 115, 1, 73, 57, 110, 48, -116 };
 	}
 
 	@Override
 	public String getPublicKey() {
 		return mKeyPublicBilling;
 	}
 
 	@Override
 	public void onBillingChecked(boolean supported) {
 		if (supported) {
 			restoreTransactions();
 		} else {
 			showDialog(R.string.billing_not_supported_message);
 		}
 	}
 
 	@Override
 	public void restoreTransactions() {
 		BillingController.restoreTransactions(this);
 		checkPurchase();
 
 	}
 
 	private void checkPurchase() {
 		if (mIdProduct != null) {
 			List<Transaction> transactions = BillingController.getTransactions(this.getBaseContext(), mIdProduct);
 			if (transactions.isEmpty()) {
 				showPopup();
 			}
 			for (Transaction transaction : transactions) {
 				if (transaction.purchaseState == PurchaseState.PURCHASED) {
 					finish();
 				} else {
 					canceledApp();
 					showPopup();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onPurchaseStateChanged(String id_product, PurchaseState status) {
 		if (id_product.equalsIgnoreCase(mIdProduct) && status == PurchaseState.PURCHASED) {
 			purchasedApp();
 			mExitButton.setVisibility(Button.VISIBLE);
 			mBuyButton.setVisibility(Button.GONE);
 			mTextView.setText(R.string.popup_text_unlock);
 		} else {
 			canceledApp();
 		}
 	}
 
 	@Override
 	public void onRequestPurchaseResponse(String id_product, ResponseCode code) {
 		if (id_product.equalsIgnoreCase(mIdProduct) && code == ResponseCode.RESULT_OK) {
 			purchasedApp();
 			mExitButton.setVisibility(Button.VISIBLE);
 			mBuyButton.setVisibility(Button.GONE);
 			mTextView.setText(R.string.popup_text_unlock);
 		} else {
 			canceledApp();
 		}
 	}
 
 
 	private void purchasedApp() {
 		SharedPreferences.Editor prefEditor = mSharedPreferences.edit();
 		prefEditor.putBoolean(mKeyPrefBilling, true);
 		prefEditor.commit();
 	}
 
 	private void canceledApp() {
 		SharedPreferences.Editor prefEditor = mSharedPreferences.edit();
 		prefEditor.putBoolean(mKeyPrefBilling, false);
 		prefEditor.commit();
 	}
}
