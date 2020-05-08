 package maze.gui;
 
 import java.awt.Point;
 
 import maze.ai.LeftWallFollower;
 import maze.ai.RobotController;
 import maze.model.RobotModelMaster;
 
 /**
  * Animates a MicroMouse robot against a maze view.
  * @author Luke Last
  */
 public final class RobotAnimator extends Thread
 {
    private volatile boolean isRunning = true;
    private final MazeView view;
    private final RobotController controller;
    /**
     * The action connected to
     */
    private final Runnable finishedCallback;
    /**
     * The time to sleep between rendering frames.
     */
   private int sleepTime = 1000 / 60;
 
    /**
     * Number of frames of animation to display between steps.
     */
    private int movesPerStep = 10;
 
    /**
     * Constructor.
     * @param mazeView
     * @param finishedCallback
     */
    public RobotAnimator(final MazeView mazeView, Runnable finishedCallback)
    {
       this.view = mazeView;
       this.finishedCallback = finishedCallback;
       this.controller = new RobotController(this.view.getModel(), new LeftWallFollower());
       super.setDaemon(true);
    }
 
    @Override
    public void run()
    {
       final RobotModelMaster model = this.controller.getRobotModelMaster();
       this.view.setRobotPosition(this.view.getCellCenter(model.getCurrentLocation()),
                                  model.getDirection().getRadians());
       while (this.isRunning && this.controller.isRobotDone() == false)
       {
          try
          {
             //Get the robots current position.
             final Point srcLocation = this.view.getCellCenter(model.getCurrentLocation());
            final double srcRotation = model.getDirection().getRadians();
 
             controller.nextStep(); //Move robot.
 
             //Get the robots new position.
             final Point destLocation = this.view.getCellCenter(model.getCurrentLocation());
            final double destRotation = model.getDirection().getRadians();
 
             //Increment is fraction at a time to the destination position.
             for (int inc = 1; inc <= this.movesPerStep; inc++)
             {
                final double percentage = (double) inc / this.movesPerStep;
                int x = (int) (srcLocation.x + (destLocation.x - srcLocation.x) * percentage);
                int y = (int) (srcLocation.y + (destLocation.y - srcLocation.y) * percentage);
                double rot = srcRotation + (destRotation - srcRotation) * percentage;
                this.view.setRobotPosition(new Point(x, y), rot);
                Thread.sleep(this.sleepTime);
             }
          }
          catch (Exception e)
          {
             e.printStackTrace();
             this.isRunning = false;
          }
       }
       if (this.finishedCallback != null)
       {
          this.finishedCallback.run();
       }
    }
 
    public boolean isRunning()
    {
       return isRunning;
    }
 
    /**
     * Shut this sucker down.
     */
    public void shutdown()
    {
       this.isRunning = false;
       this.interrupt();
    }
 
    public int getFPS()
    {
       return 1000 / this.sleepTime;
    }
 
    public void setFPS(int framesPerSecond)
    {
       this.sleepTime = 1000 / framesPerSecond;
    }
 
    /**
     * Get the number of intermediate frames of animation that take place between
     * each step of the robot.
     */
    public int getMovesPerStep()
    {
       return movesPerStep;
    }
 
    public void setMovesPerStep(int movesPerStep)
    {
       this.movesPerStep = movesPerStep;
    }
 
 }
