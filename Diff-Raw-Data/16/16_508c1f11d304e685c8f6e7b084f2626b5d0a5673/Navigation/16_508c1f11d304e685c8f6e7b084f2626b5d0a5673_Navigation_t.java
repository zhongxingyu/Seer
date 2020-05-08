 package com.fusionx.tilal6991.multiboot;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class Navigation extends SherlockFragmentActivity implements
 		ActionBar.TabListener {
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(final FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public int getCount() {
 			if (gappsTime == true)
 				return 1;
 			else
 				return 3;
 		}
 
 		@Override
 		public Fragment getItem(final int i) {
 			switch (i) {
 			case 0:
 				if (gappsTime == true)
 					return new Fragments.Finalisation();
 				else
 					return new Fragments.SystemPartition();
 			case 1:
 				return new Fragments.DataPartition();
 			case 2:
 				return new Fragments.Finalisation();
 			}
 			return null;
 		}
 
 		@Override
 		public CharSequence getPageTitle(final int position) {
 			switch (position) {
 			case 0:
 				if (gappsTime == true)
 					return getString(R.string.title_section3).toUpperCase();
 				else
 					return getString(R.string.title_section1).toUpperCase();
 			case 1:
 				return getString(R.string.title_section2).toUpperCase();
 			case 2:
 				return getString(R.string.title_section3).toUpperCase();
 			}
 			return null;
 		}
 	}
 
 	static final int DIALOG_LACK_OF_SYSTEM_IMAGE = 0;
 
 	boolean createDataImage = true;
 
 	boolean createSystemImage = true;
 
 	String dataImageName;
 	String dataImageSize;
 	boolean gappsTime = false;
 	private String mChosen;
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	ViewPager mViewPager;
 	String systemImageName;
 
 	String systemImageSize;
 
 	public void changeEnableData() {
 		if (!(findViewById(R.id.checkBox1) == null)) {
 			int visible;
 			createDataImage = ((CheckBox) findViewById(R.id.checkBox1))
 					.isChecked();
 			if (!createDataImage)
 				visible = 4;
 			else
 				visible = 0;
 			findViewById(R.id.editText1).setVisibility(visible);
 			findViewById(R.id.editText2).setVisibility(visible);
 			findViewById(R.id.textView1).setVisibility(visible);
 			findViewById(R.id.textView2).setVisibility(visible);
 		}
 	}
 
 	public void changeEnableData(final View view) {
 		changeEnableData();
 	}
 
 	public void changeEnableSystem() {
 		if (!(findViewById(R.id.checkBox1system) == null)) {
 			int visible;
 			createSystemImage = ((CheckBox) findViewById(R.id.checkBox1system))
 					.isChecked();
 			if (!createSystemImage)
 				visible = 4;
 			else
 				visible = 0;
 			findViewById(R.id.editText1system).setVisibility(visible);
 			findViewById(R.id.editText2system).setVisibility(visible);
 			findViewById(R.id.textView1system).setVisibility(visible);
 			findViewById(R.id.textView2system).setVisibility(visible);
 		}
 	}
 
 	public void changeEnableSystem(final View view) {
 		changeEnableSystem();
 	}
 
 	private void chooseRom(final File mPath) {
 		final FilenameFilter filter = new FilenameFilter() {
 			@Override
 			public boolean accept(final File dir, final String filename) {
 				return filename.endsWith(".zip")
 						|| new File(dir.getAbsolutePath() + "/" + filename)
 								.isDirectory();
 			}
 		};
 		final String[] mFileList = mPath.list(filter);
 		java.util.Arrays.sort(mFileList, String.CASE_INSENSITIVE_ORDER);
 
 		final Builder builder = new Builder(this);
 		builder.setTitle("Choose your ROM");
 
 		final DialogInterface.OnClickListener k = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(final DialogInterface dialog, final int which) {
 				mChosen = mFileList[which];
 				final File file = new File(mPath + "/" + mChosen);
 				if (file.isDirectory())
 					chooseRom(file);
 				else {
 					((TextView) findViewById(R.id.txtRom)).setText(file
 							.getAbsolutePath());
 					findViewById(R.id.button1).setEnabled(true);
 					return;
 				}
 			}
 		};
 		builder.setItems(mFileList, k);
 		builder.show();
 	}
 
 	public void chooseRom(final View v) {
 		chooseRom(Environment.getExternalStorageDirectory());
 	}
 
 	public void finish(final View view) {
 		final String systemImage = ((EditText) findViewById(R.id.edtSystem))
 				.getText().toString();
 		final String dataImage = ((EditText) findViewById(R.id.edtData))
 				.getText().toString();
 		Intent intent = null;
 		if (gappsTime == true) {
 			intent = new Intent(this, CreateOther.class);
 			if (new File(Environment.getExternalStorageDirectory()
 					+ "/multiboot/" + systemImage).exists())
 				intent.putExtra("systemimagename", systemImage);
 			else {
 				onCreateDialog(DIALOG_LACK_OF_SYSTEM_IMAGE);
 				return;
 			}
 		} else {
 			intent = new Intent(this, CreateRom.class);
 			if (!createDataImage) {
 				if (new File(Environment.getExternalStorageDirectory()
 						+ "/multiboot/" + dataImage).exists())
 					intent.putExtra("dataimagename", dataImage);
 				else {
 					onCreateDialog(DIALOG_LACK_OF_SYSTEM_IMAGE);
 					return;
 				}
 			} else {
 				intent.putExtra("createdataimage", true);
 				intent.putExtra("dataimagename", dataImageName);
 				intent.putExtra("dataimagesize", dataImageSize);
 			}
 			if (!createSystemImage) {
 				if (new File(Environment.getExternalStorageDirectory()
 						+ "/multiboot/" + systemImage).exists())
 					intent.putExtra("systemimagename", systemImage);
 				else {
 					onCreateDialog(DIALOG_LACK_OF_SYSTEM_IMAGE);
 					return;
 				}
 			} else {
 				intent.putExtra("createsystemimage", true);
 				intent.putExtra("systemimagename", systemImageName);
 				intent.putExtra("systemimagesize", systemImageSize);
 			}
 		}
 		intent.putExtra("filename", mChosen);
		intent.putExtra("filePath", ((TextView) findViewById(R.id.txtRom)).getText());
 		intent.putExtras(getIntent().getExtras());
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_navigation);
 		gappsTime = getIntent().getExtras().getBoolean("gapps");
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		final ActionBar actionBar = getSupportActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(final int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 	}
 
 	@Override
 	protected Dialog onCreateDialog(final int id) {
 		final Dialog dialog = null;
 		switch (id) {
 		case DIALOG_LACK_OF_SYSTEM_IMAGE:
 			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("One of the images doesn't exist!")
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										final DialogInterface dialog,
 										final int id) {
 									return;
 								}
 							});
 			final AlertDialog alert = builder.create();
 			alert.show();
 			break;
 		}
 		return dialog;
 	}
 
 	@Override
 	public void onTabReselected(final ActionBar.Tab tab,
 			final FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabSelected(final ActionBar.Tab tab,
 			final FragmentTransaction fragmentTransaction) {
 		mViewPager.setCurrentItem(tab.getPosition());
 		switch (tab.getPosition()) {
 		case 0:
 			changeEnableSystem();
 		case 1:
 			changeEnableData();
 		case 2:
 			if (gappsTime == true) {
 				if (!(findViewById(R.id.edtSystem) == null)) {
 					findViewById(R.id.edtSystem).setVisibility(0);
 					findViewById(R.id.txtSystem).setVisibility(0);
 				}
 				if (!(findViewById(R.id.edtData) == null)) {
 					findViewById(R.id.edtData).setVisibility(4);
 					findViewById(R.id.txtData).setVisibility(4);
 				}
 			} else {
 				if (!(findViewById(R.id.checkBox1system) == null)
 						&& !(findViewById(R.id.edtSystem) == null)) {
 					int systemImage;
 					if (((CheckBox) findViewById(R.id.checkBox1system))
 							.isChecked()) {
 						systemImage = 4;
 						systemImageSize = ((EditText) findViewById(R.id.editText1system))
 								.getText().toString();
 						systemImageName = ((EditText) findViewById(R.id.editText2system))
 								.getText().toString();
 					} else
 						systemImage = 0;
 					findViewById(R.id.edtSystem).setVisibility(systemImage);
 					findViewById(R.id.txtSystem).setVisibility(systemImage);
 				}
 				if (!(findViewById(R.id.checkBox1) == null)
 						&& !(findViewById(R.id.edtData) == null)) {
 					int dataImage;
 					if (((CheckBox) findViewById(R.id.checkBox1)).isChecked()) {
 						dataImage = 4;
 						dataImageSize = ((EditText) findViewById(R.id.editText1))
 								.getText().toString();
 						dataImageName = ((EditText) findViewById(R.id.editText2))
 								.getText().toString();
 					} else
 						dataImage = 0;
 					findViewById(R.id.edtData).setVisibility(dataImage);
 					findViewById(R.id.txtData).setVisibility(dataImage);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onTabUnselected(final ActionBar.Tab tab,
 			final FragmentTransaction fragmentTransaction) {
 	}
 }
