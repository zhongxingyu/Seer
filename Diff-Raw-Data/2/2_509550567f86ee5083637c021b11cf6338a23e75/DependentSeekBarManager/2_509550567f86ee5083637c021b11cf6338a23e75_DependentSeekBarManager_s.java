 package com.dependentseekbars;
 
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Build;
 import android.util.AttributeSet;
 import android.widget.LinearLayout;
 
 import com.dependentseekbars.DependencyGraph.Node;
 
 /**
  * A DependentSeekBarManager is a collection of {@link DependentSeekBar}s which
  * allows you to create relationships so that the progress of one
  * {@link DependentSeekBar} can affect the progress of other
  * {@link DependentSeekBar}s. Less than and greater than relationships can be
  * created between different {@link DependentSeekBar}s, such that one
  * DependentSeekBar must always be less/greater than another.
  * 
  * @author jbeveridge and sujen
  * 
  */
 public class DependentSeekBarManager extends LinearLayout {
     private ArrayList<DependentSeekBar> seekBars;
     private ArrayList<ArrayList<Integer>> minDependencies;
     private ArrayList<ArrayList<Integer>> maxDependencies;
     private DependencyGraph dg;
     private int spacing = 0;
     private boolean shiftingAllowed = true;
 
     private final int DEFAULT_MAXIMUM_PROGRESS = 100;
 
     /**
      * Creates a DependentSeekBarManager that can be used to contain
      * {@link DependentSeekBar}s. By default, the DependentSeekBarManager has a
      * vertical orientation.
      * 
      * @param context the application environment
      */
     public DependentSeekBarManager(Context context) {
         super(context);
 
         init();
     }
 
     /**
      * Creates a DependentSeekBarManager that can be used to contain
      * {@link DependentSeekBar}s. By default, the DependentSeekBarManager has a
      * vertical orientation.
      * 
      * @param context the application environment
      * @param attrs the layout attributes
      */
     public DependentSeekBarManager(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.DependentSeekBarManager);
 
         spacing = a.getInt(R.styleable.DependentSeekBarManager_spacing, 0);
 
         a.recycle();
 
         init();
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public DependentSeekBarManager(Context context, AttributeSet attrs,
             int defStyle) {
         super(context, attrs, defStyle);
 
         TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.DependentSeekBarManager, defStyle, 0);
 
         spacing = a.getInt(R.styleable.DependentSeekBarManager_spacing, 0);
 
         a.recycle();
 
         init();
     }
 
     private void init() {
         seekBars = new ArrayList<DependentSeekBar>();
         minDependencies = new ArrayList<ArrayList<Integer>>();
         maxDependencies = new ArrayList<ArrayList<Integer>>();
         dg = new DependencyGraph();
 
         setOrientation(LinearLayout.VERTICAL);
 
         if (isInEditMode()) {
             for (int i = 0; i < 20; i++) {
                 createSeekBar(i);
             }
 
         }
     }
 
     /**
      * Create a new {@link DependentSeekBar} and adds it to the widget. The
      * {@link DependentSeekBar} returned will be shown at the bottom of the
      * widget if in vertical view, and on the right if in horizontal view. The
      * maximum value progress will be set to 100.
     * 
      * @param progress the initial progress of the seek bar
      * @return the {@link DependentSeekBar} which was added to the widget
      *
      * @see #createSeekBar(int, int)
      */
     public DependentSeekBar createSeekBar(int progress) {
         return createSeekBar(progress, DEFAULT_MAXIMUM_PROGRESS);
     }
 
     /**
      * Create a new {@link DependentSeekBar} and adds it to the widget. The
      * {@link DependentSeekBar} returned will be shown at the bottom of the
      * widget if in vertical view, and on the right if in horizontal view.
      * 
      * @param progress the initial progress of the seek bar
      * @param maximum the maximum value which the progress can be set to
      * @return the {@link DependentSeekBar} which was added to the widget
      * 
      * @see #createSeekBar(int)
      */
     public DependentSeekBar createSeekBar(int progress, int maximum) {
         DependentSeekBar seekBar = new DependentSeekBar(getContext(), this,
                 progress, maximum);
         seekBars.add(seekBar);
 
         minDependencies.add(new ArrayList<Integer>());
         maxDependencies.add(new ArrayList<Integer>());
 
         // Create a new node for the seek bar and add it to the dependency graph
         Node node = dg.addSeekBar(seekBar);
         seekBar.setNode(node);
 
         addView(seekBar);
         setupMargins();
 
         return seekBar;
 
     }
 
     private void setupMargins() {
         if (seekBars.size() > 1) {
             setSeekBarMargin(seekBars.size() - 2, spacing);
         }
         setSeekBarMargin(seekBars.size() - 1, 0);
     }
 
     private void setSeekBarMargin(int index, int space){
         ((LinearLayout.LayoutParams) seekBars.get(index).getLayoutParams()).setMargins(
                 0, 0, 0, space);
     }
 
     /**
      * Adds a {@link DependentSeekBar} to the manager allowing the
      * {@link DependentSeekBar} to have dependency relationships with other
      * {@link DependentSeekBar}s in the manager
      *
      * @param seekBar
      */
     public void addSeekBar(DependentSeekBar seekBar) {
         if(seekBars.contains(seekBar))
             return;
 
         seekBars.add(seekBar);
 
         minDependencies.add(new ArrayList<Integer>());
         maxDependencies.add(new ArrayList<Integer>());
 
         Node node = dg.addSeekBar(seekBar);
         seekBar.setNode(node);
         seekBar.setManager(this);
     }
 
     public DependentSeekBar getSeekBar(int index) {
         if (index < 0 || index >= seekBars.size()) {
             return null;
         }
 
         return seekBars.get(index);
     }
 
     /**
      * Removes the DependentSeekBar at index from the widget. The index values
      * correspond to the DependentSeekBar's visual location (with the top bar
      * being 0 and increasing downwards, or in horizontal left being 0 and right
      * being n-1). When a bar is removed, the index values of the
      * DependentSeekBars are adjusted to represent their new visual locations.
      * 
      * @param index the index of the {@link DependentSeekBar} to remove
      * @param restructureDependencies
      * @return true iff there is a {@link DependentSeekBar} with given index and
      *         it is successfully removed
      * 
      * @see #removeSeekBar(DependentSeekBar, boolean)
      */
     public boolean removeSeekBar(int index, boolean restructureDependencies) {
         if (index >= seekBars.size() || index < 0)
             return false;
 
         dg.removeSeekBar(seekBars.get(index), restructureDependencies);
         seekBars.remove(index);
         setupMargins();
         return true;
     }
 
     /**
      * Removes the given DependentSeekBar from the widget. When a bar is
      * removed, the index values of the DependentSeekBars are adjusted to
      * represent their new visual locations.
      * 
      * @param rsb the DependentSeekBar to remove
      * @param restructureDependencies
      * @return true iff the given DependentSeekBar is contained in the widget
      *         and it is successfully removed
      * 
      * @see #removeSeekBar(int, boolean)
      */
     public boolean removeSeekBar(DependentSeekBar dependent,
             boolean restructureDependencies) {
         for (DependentSeekBar seekBar : seekBars) {
             if (seekBar.equals(dependent)) {
                 dg.removeSeekBar(dependent, restructureDependencies);
                 seekBars.remove(seekBar);
                 setupMargins();
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Add dependencies between the DependentSeekBar at dependentIndex and the
      * DependentSeekBars at limitingIndices. The dependencies will ensure that
      * the progress of the DependentSeekBar at dependentIndex is always less
      * than the progress of the DependentSeekBars at each of limitingIndices.
      * The index values correspond to the DependentSeekBar's visual location
      * (with the top bar being 0 and increasing downwards, or in horizontal left
      * being 0 and right being n-1).
      * 
      * @param dependentIndex the index of the DependentSeekBar which must have
      *        the smaller progress
      * @param limitingIndices the indices of the DependentSeekBars which must
      *        have greater progresses than the dependent DependentSeekBar
      * @return
      */
     void addLessThanDependencies(DependentSeekBar dependentSeekBar,
             int[] limitingIndices) {
         checkIndices(limitingIndices);
         dg.addLessThanDependencies(dependentSeekBar,
                 getSubclassedSeekBars(limitingIndices));
     }
 
     void addLessThanDependencies(DependentSeekBar dependentSeekBar,
             DependentSeekBar[] limiting) {
 
         for (DependentSeekBar limit : limiting) {
             if (limit == null || !seekBars.contains(limit))
                 throw new NullPointerException();
         }
         dg.addLessThanDependencies(dependentSeekBar, getSubclassedSeekBars(limiting));
     }
 
     /**
      * Add dependencies between the DependentSeekBar at dependentIndex and the
      * DependentSeekBars at limitingIndices. The dependencies will ensure that
      * the progress of the DependentSeekBar at dependentIndex is always greater
      * than the progress of the DependentSeekBars at each of limitingIndices.
      * The index values correspond to the DependentSeekBar's visual location
      * (with the top bar being 0 and increasing downwards, or in horizontal left
      * being 0 and right being n-1).
      * 
      * @param dependentIndex the index of the DependentSeekBar which must have
      *        the smaller progress
      * @param limitingIndices the indices of the DependentSeekBars which must
      *        have greater progresses than the dependent DependentSeekBar
      * @return
      */
     void addGreaterThanDependencies(DependentSeekBar dependentSeekBar,
             int[] limitingIndices) {
         checkIndices(limitingIndices);
         dg.addGreaterThanDependencies(dependentSeekBar,
                 getSubclassedSeekBars(limitingIndices));
     }
 
     void addGreaterThanDependencies(DependentSeekBar dependentSeekBar,
             DependentSeekBar[] limiting) {
 
         for (DependentSeekBar limit : limiting) {
             if (limit == null || !seekBars.contains(limit))
                 throw new NullPointerException();
         }
 
         dg.addGreaterThanDependencies(dependentSeekBar, getSubclassedSeekBars(limiting));
     }
 
     private DependentSeekBar[] getSubclassedSeekBars(
             DependentSeekBar[] dependentSeekBars) {
         DependentSeekBar[] limitingSeekBars = new DependentSeekBar[dependentSeekBars.length];
         for (int i = 0; i < dependentSeekBars.length; i++) {
             limitingSeekBars[i] = dependentSeekBars[i];
         }
 
         return limitingSeekBars;
     }
 
     private DependentSeekBar[] getSubclassedSeekBars(int[] indices) {
         DependentSeekBar[] limitingSeekBars = new DependentSeekBar[indices.length];
         for (int i = 0; i < indices.length; i++) {
             limitingSeekBars[i] = seekBars.get(indices[i]);
         }
 
         return limitingSeekBars;
     }
 
     private void checkIndices(int[] indices) {
         for (int i = 0; i < indices.length; i++) {
             if (indices[i] >= seekBars.size() || indices[i] < 0)
                 throw new IndexOutOfBoundsException();
         }
     }
 
     /**
      * When shifting is enabled, the widget will attempt to move other seek bars
      * which are dependent on seek bar being adjusted and are blocking its path.
      * 
      * @return true iff shifting is currently allowed
      * 
      * @see #setShiftingAllowed(boolean)
      */
     public boolean isShiftingAllowed() {
         return shiftingAllowed;
     }
 
     /**
      * Set the value of shifting. When shifting is enabled, the widget will
      * attempt to move other seek bars which are dependent on seek bar being
      * adjusted and are blocking its path.
      * 
      * @param b
      * 
      * @see #isShiftingAllowed()
      */
     public void setShiftingAllowed(boolean b) {
         shiftingAllowed = b;
     }
 
 }
