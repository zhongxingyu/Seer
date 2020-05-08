 package widgets;
 
 import cansocket.*;
 
 import java.lang.reflect.*;
 
 public class NodeEnableLabel extends NodeStateLabel
 {
 	protected final int id;
 	protected static final int length = 8192;
 	protected final int[] times = new int[length];
 
 	protected int next_out, next_in;
 	protected int last_time;
 
 	public NodeEnableLabel(String name, int bit)
 		throws NoSuchFieldException, IllegalAccessException
 	{
 		super(name, bit);
 		id = CanBusIDs.class.getField(name).getInt(null);
 		update(0);
 	}
 
 	public void message(CanMessage msg)
 	{
 		super.message(msg);
 
 		int now = msg.getTimestamp();
 		if(msg.getId() == id)
 			times[next_in++ & (length - 1)] = now;
 
		if(now - last_time >= 25)
 		{
 			update(now);
 			last_time = now;
 		}
 	}
 
 	private void update(int now)
 	{
 		final int tenths;
 		while(next_out != next_in && times[next_out & (length - 1)] <= now - 300)
 			++next_out;
 
		if(next_in == next_out)
 			tenths = 0;
 		else
 			tenths = (int) (1000f * (next_in - next_out) / (now - times[next_out & (length - 1)]));
 		setDetail("" + (tenths / 10f) + "/sec");
 	}
 }
