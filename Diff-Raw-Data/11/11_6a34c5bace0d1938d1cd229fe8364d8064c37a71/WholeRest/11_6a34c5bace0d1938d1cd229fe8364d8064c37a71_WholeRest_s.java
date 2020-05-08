 package crescendo.sheetmusic;
 
 import java.awt.Graphics;
 
 import crescendo.base.song.Note;
 
 public class WholeRest extends DrawableNote{
 	
 	public WholeRest() {}
 	
 	public WholeRest(Note n,int x,int y)
 	{
 		super(n,x,y);
 		
 		noteType = NoteType.WHOLENOTE;
		
		if(this.note.getPitch()>=60)
		{
			this.y = y+yPositionOfNote(73);
		}
		else
		{
			this.y = y+yPositionOfNote(53);
		}
		
		
 	}
 	
 	public DrawableNote spawn(Note n,int x,int y)
 	{
 		return new WholeRest(n,x,y);
 	}
 	
 	public int getWidth() {
 		return 8;
 	}
 	
 	public double getBeatsCovered() {
 		return 4;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(color);
 		g.fillRect(x,y,8,4);
 	}
 }
