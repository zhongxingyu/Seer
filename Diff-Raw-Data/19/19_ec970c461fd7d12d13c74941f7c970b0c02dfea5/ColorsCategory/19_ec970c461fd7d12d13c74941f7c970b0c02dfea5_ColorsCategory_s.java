 package bu.edu.cs673.edukid.db.model.category;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import bu.edu.cs673.edukid.R;
 import bu.edu.cs673.edukid.db.Database;
 import bu.edu.cs673.edukid.db.defaults.DatabaseDefaults;
 import bu.edu.cs673.edukid.db.model.Color;
 import bu.edu.cs673.edukid.db.model.Word;
 
 @SuppressWarnings("serial")
 public class ColorsCategory implements CategoryType {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getCategoryName() {
 		return "Colors";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getCategoryId() {
 		return Category.COLORS.ordinal();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Drawable getCategoryImage(Context context) {
 		return context.getResources().getDrawable(R.drawable.colorsnew);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String[] getItems() {
 		// TODO: don't hardcode
 		return DatabaseDefaults.getColors();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getItem(int itemIndex) {
 		// TODO
 		return DatabaseDefaults.getColors()[itemIndex];
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getItemCount() {
 		return DatabaseDefaults.getColors().length;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getItemPhoneticSound(int itemIndex) {
 		// TODO
 		Color col = Database.getInstance().getColors().get(itemIndex);
 
 		if (col != null) {
 			String colourSound = col.getColourSound();
 
 			if (colourSound != null && !colourSound.isEmpty()) {
 				return colourSound;
 			}
 		}
 
 		return DatabaseDefaults.getDefaultColorPhoneticSounds()[itemIndex];
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Word[] getSettingsItemWords(int itemIndex) {
 		// TODO
 		return DatabaseDefaults.getDefaultColorWords()[itemIndex];
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Word getSettingsItemWord(int itemIndex, int wordIndex) {
 		// TODO
 		return DatabaseDefaults.getDefaultColorWords()[itemIndex][wordIndex];
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Word[] getLearnItemWords(int itemIndex) {
 		// TODO: for now, getting all the words. fix this.
 		return getSettingsItemWords(itemIndex);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Word getLearnItemWord(int itemIndex, int wordIndex) {
 		return getLearnItemWords(itemIndex)[wordIndex];
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getLearnItemWordCount(int itemIndex) {
 		// TODO
 		return DatabaseDefaults.getDefaultColorWords()[itemIndex].length;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addItemWord(int itemIndex, Word word) {
 		// TODO: Jasjot, implement this
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void editItemWord(int itemIndex, int wordIndex, Word word) {
 		// TODO: Jasjot, implement this
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getSettingsItemDrawableId(int itemIndex, int imageIndex) {
 		// TODO
 		return DatabaseDefaults.getDefaultColorWords()[itemIndex][imageIndex]
 				.getDrawableId();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getLearnItemDrawableId(int itemIndex, int imageIndex) {
		// TODO
		return 0;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getItemTextSize() {
 		return 50;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean canAddItems() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean hasGameMode() {
 		return false;
 	}
 }
