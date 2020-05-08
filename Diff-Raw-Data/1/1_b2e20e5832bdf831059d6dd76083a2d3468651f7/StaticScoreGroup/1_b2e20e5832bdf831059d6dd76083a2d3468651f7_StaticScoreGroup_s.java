 package YahtzeeGame.Components;
 
 import YahtzeeGame.Categories.Category;
 
 /**
  * Class representing a score group that is chosen by default. Such examples
  * include totals and bonuses that only provide information and are not
  * conventional categories.
  * 
  * @author Ryan Harrison
  * 
  */
 public class StaticScoreGroup extends ScoreGroup implements Resettable
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Create a new static score group. By default the Category is 'Total' and
 	 * the group has already been chosen. The score is displayed as '0' by
 	 * default
 	 */
 	public StaticScoreGroup()
 	{
 		super(Category.getCategory(14));
 		this.chosen = true;
 		this.setTextToCategory();
 		this.score.setText("0");
 	}
 
 	/**
 	 * Create a new static score group with specified name. This name will
 	 * override that of the category.
 	 * 
 	 * @param name
 	 *            The name of this score group
 	 */
 	public StaticScoreGroup(String name)
 	{
 		this();
 		this.categoryName = name;
 	}
 
 	/**
 	 * Reset the state of this score group. Set the text to that of the category
 	 * and set the score to '0'.
 	 */
 	@Override
 	public void reset()
 	{
 		super.reset();
 		this.setTextToCategory();
 		this.score.setText("0");
 	}
 
 	/**
 	 * Set the score of this score group
 	 * 
 	 * @param score
 	 *            The new score for this group
 	 */
 	public void setScore(int score)
 	{
 		this.score.setText(Integer.toString(score));
 	}
 }
