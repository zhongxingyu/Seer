 package com.greenteam.huntjumper.model.bonuses;
 
 import com.greenteam.huntjumper.model.Jumper;
 
 import java.util.List;
 
 /**
  * User: GreenTea Date: 21.07.12 Time: 16:18
  */
 public abstract class AbstractBonusEffect implements IJumperBonusEffect
 {
    private int duration;
    protected int timeLeft;
    protected Jumper jumper;
    protected List<Jumper> otherJumpers;
 
    public AbstractBonusEffect(int duration, int timePassed)
    {
      this.duration = duration - timePassed;
    }
 
    protected String getEffectName()
    {
       return getClass().getSimpleName();
    }
 
    @Override
    public void signalTimeLeft(int timeLeft)
    {
       this.timeLeft = timeLeft;
    }
 
    @Override
    public final void onStartEffect(Jumper jumper, List<Jumper> otherJumpers)
    {
       this.jumper = jumper;
       this.otherJumpers = otherJumpers;
       timeLeft = getDuration();
       onStartEffect();
    }
 
    @Override
    public int getDuration()
    {
       return duration;
    }
 
    public abstract void onStartEffect();
 }
