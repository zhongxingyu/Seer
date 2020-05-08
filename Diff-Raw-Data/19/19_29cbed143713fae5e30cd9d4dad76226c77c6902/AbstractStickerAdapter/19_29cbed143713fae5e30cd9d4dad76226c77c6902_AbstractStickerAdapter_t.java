 package at.almeida.mypanini.adapters;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 import at.almeida.mypanini.R;
 import at.almeida.mypanini.model.StickerAlbumMemory;
 
 public abstract class AbstractStickerAdapter extends BaseAdapter implements ListAdapter{
 	private Activity context;
 
 	private int size;
 	private StickerAlbumDbAdapter stickerDbAdapter;
 
 	private StickerAlbumMemory album;
 
 	private Typeface font;
 
 	
 	
 	public StickerAlbumDbAdapter getStickerDbAdapter() {
 		return stickerDbAdapter;
 	}
 
 	public StickerAlbumMemory getAlbum() {
 		return album;
 	}
 
 	public Long getAlbumId() {
		if(album != null){
			return album.getAlbumId();
		}
		else
			return null;
 	}
 
 	public void setAlbumId(Long albumId) {
 		album.setAlbumId(albumId);
 	}
 
 	public Typeface getFont() {
 		return font;
 	}
 
 	public void setFont(Typeface font) {
 		this.font = font;
 	}
 
 	public AbstractStickerAdapter(Activity context) {
 		buildFromAlbumId(context, null);
 	}
 
 	public AbstractStickerAdapter(Activity context, Long albumId) {
 		
 		buildFromAlbumId(context, albumId);
 	}
 
 	public void populateModel(Long albumId){
 		Cursor stickers = retrieveStickersFromDb(albumId);
 
 		size = stickers.getCount();
 		album = new StickerAlbumMemory(size);
 		setAlbumId(albumId);
 
 		stickers.moveToFirst();
 		int numberIndex = stickers
 				.getColumnIndex(StickerAlbumDbAdapter.KEY_NUMBER);
 		int idIndex = stickers
 				.getColumnIndex(StickerAlbumDbAdapter.KEY_ID);
 		int countIndex = stickers
 				.getColumnIndex(StickerAlbumDbAdapter.KEY_COUNT);
 		while (stickers.isAfterLast() == false) {
 			int position = stickers.getPosition();
 			album.setStickerAtPosition(position,
 					stickers.getString(numberIndex));
 
 			album.setStickerIdAtPosition(position,
 					stickers.getLong(idIndex));
 
 			album.setStickerCountAtPosition(position,
 					stickers.getInt(countIndex));
 
 			stickers.moveToNext();
 		}
 	}
 	private void buildFromAlbumId(Activity context, Long albumId) {
 		stickerDbAdapter = new StickerAlbumDbAdapter(context);
 		stickerDbAdapter.open();
 		if (albumId != null) {
 			
 			populateModel(albumId);
 		}
 
 		this.context = context;
 
 	}
 
 
 	public int getCount() {
 		return size;
 	}
 
 	/**
 	 * Returns the sticker count at position. The information is stored in an
 	 * array of this class
 	 * 
 	 * @param position
 	 * @return
 	 */
 	public int getStickerCountAtPosition(int position) {
 		return album.getStickerCountAtPosition(position);
 	}
 
 	public String getItem(int position) {
 		return album.getStickerAtPosition(position);
 	}
 
 	/**
 	 * Returns the sticker id at position. The information is stored in an array
 	 * of this class
 	 * 
 	 * @param position
 	 * @return
 	 */
 	public long getItemId(int position) {
 		return album.getStickerIdAtPosition(position);
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View myView = convertView;
 
 		if (convertView == null) {
 			/* we define the view that will display on the grid */
 
 			// Inflate the layout
 			LayoutInflater li = context.getLayoutInflater();
 			myView = li.inflate(R.layout.missing_item_gridview, null, true);
 		}
 		// Add The Text!!!
 		TextView tv = (TextView) myView
 				.findViewById(R.id.missing_item_grid_text);
 		// Change font if it has been set
 		if (font != null) {
 			tv.setTypeface(font);
 		}
 
 		tv.setText(album.getStickerAtPosition(position));
 
 		return myView;
 	}
 
 	public void changeFont(Typeface findFont) {
 		font = findFont;
 
 	}
 
 
 	// TODO - Might be better off at StickerAdapter
 	public boolean iscurrentlySelected(int position) {
 		if (album.getStickerCountAtPosition(position) > 0)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * Changes the sticker's text view to visually show we have it
 	 * 
 	 * @param tv
 	 */
 	public void markAsHaveIt(View view) {
 		ImageView iv = (ImageView) view
 				.findViewById(R.id.missing_item_grid_image);
 		iv.setImageResource(R.drawable.scratched);
 		iv.setVisibility(View.VISIBLE);
 
 	}
 
 	/**
 	 * Changes the sticker's text view to visually show we DON'T have it
 	 * 
 	 * @param tv
 	 */
 	public void markAsDontHaveIt(View view) {
 		ImageView iv = (ImageView) view
 				.findViewById(R.id.missing_item_grid_image);
 		iv.setVisibility(View.GONE);
 
 	}
 
 	public abstract Cursor retrieveStickersFromDb(Long albumId);
 	public abstract void updateCurrentSelection(View v, int position,
 			long stickerToChangeId);
 
 }
