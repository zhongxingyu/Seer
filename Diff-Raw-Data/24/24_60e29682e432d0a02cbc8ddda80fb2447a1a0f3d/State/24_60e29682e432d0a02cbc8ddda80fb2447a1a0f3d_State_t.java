 public class State
 {
    private final State m_past;
    private State m_future;
     
     private Vector3D m_value;
     private int m_assist;
     
     public State(State past, State future, int hints, Vector3D value)
     {
        m_past = past;
        m_future = future;
         m_value = value;
     }
     
     public void setFuture(State future)
     {
        m_future = future;
     }
     
     public State getPast()
     {
        return m_past;
     }
     
     public State getFuture()
     {
        return m_future;
     }
     
     public Vector3D getValue()
     {
         return m_value;
     }
     
     public int getAssist()
     {
         return m_assist;
     }
     
     public void loseAssist()
     {
         subtractAssist(true);
     }
     
     private void subtractAssist(boolean backward)
     {
         --m_assist;
         
         if (backward == true)
         {
             if (m_past != null)
             {
                 m_past.subtractAssist(true);
             }
         }
         
         if (m_future != null)
         {
             m_future.subtractAssist(false);
         }
     }
 }
