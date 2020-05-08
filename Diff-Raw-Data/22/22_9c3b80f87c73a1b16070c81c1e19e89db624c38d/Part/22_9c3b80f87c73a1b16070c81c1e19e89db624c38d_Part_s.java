 package abc.notation;
 
 /** <TT>Part</TT> objects are used to define parts in tunes. */
 public class Part
 {
   private char m_label;
  private Tune m_tune = null;
   private Tune.Score m_score = null;
 
   Part (Tune tune, char labelValue)
   {
    m_tune = tune;
     m_label = labelValue;
     m_score = tune.createScore();
   }
 
   /** Sets the label that identifies this part.
    * @param labelValue The label that identifies this part. */
   public void setLabel(char labelValue)
   { m_label = labelValue; }
 
   /** Returns the label that identifies this part.
    * @return The label that identifies this part. */
   public char getLabel()
   { return m_label; }
 
   /** Returns the score associated to this part.
    * @return The score associated to this part. */
   public Tune.Score getScore()
   {return m_score;}
 
   void setScore(Tune.Score score)
   {m_score = score;}
 }
