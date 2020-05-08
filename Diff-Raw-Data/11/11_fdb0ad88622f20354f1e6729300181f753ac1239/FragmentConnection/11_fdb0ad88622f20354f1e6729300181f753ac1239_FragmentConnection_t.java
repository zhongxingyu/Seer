 package ca.cmput301f13t03.adventure_datetime.view.treeView;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import ca.cmput301f13t03.adventure_datetime.view.treeView.Camera;
 
 /**
  * Represents a connection between two fragments
  * @author Jesse
  */
 final class FragmentConnection
 {
 	private static Paint m_pathStyle = new Paint();
 	private static boolean m_isPaintInitialized = false;
 	
 	private Path m_connectionPath = null;
 	
 	public FragmentConnection()
 	{
 		// use the provided info to build the path
 	}
 	
 	public void SetPath(Path path)
 	{
 		this.m_connectionPath = path;
 	}
 	
 	public void Draw(Canvas surface, Camera camera)
 	{
 		if(!m_isPaintInitialized)
 		{
 			SetupPaint();
 		}
 		
 		// TODO::JT add in the particle animation
 		// translate, draw then return to original point
 		camera.SetLocal(m_connectionPath);
 		surface.drawPath(m_connectionPath, m_pathStyle);
 		camera.InvertLocal(m_connectionPath);
 	}
 	
 	private static void SetupPaint()
 	{
 		m_pathStyle.setAntiAlias(true);
 		m_pathStyle.setAlpha(200);
 		m_pathStyle.setColor(Color.CYAN); // TODO::JT get a better colour!
 		m_pathStyle.setStyle(Style.STROKE);
		m_pathStyle.setStrokeWidth(3.0f);
 	}
 }
