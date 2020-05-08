 package swarm.client.managers;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.smAppContext;
 import swarm.client.entities.smCamera;
 import swarm.shared.app.smS_App;
 import swarm.shared.debugging.smU_Debug;
 import swarm.shared.entities.smA_Grid;
 import swarm.shared.structs.smPoint;
 import swarm.shared.structs.smTolerance;
 import swarm.shared.structs.smVector;
 
 public class smCameraManager
 {
 	private static final double SNAP_TOLERANCE = .00001;
 	private static final Logger s_logger = Logger.getLogger(smCameraManager.class.getName());
 	
 	private final smPoint m_cameraOrigin = new smPoint();
 	private final smPoint m_targetPosition = new smPoint();
 	private final smVector m_diffVector = new smVector();
 	private final smVector m_utilVector = new smVector();
 	private final smPoint m_utilPoint1 = new smPoint();
 	
 	private double m_startY = 0;
 	private double m_lengthToTravel = 0;
 	private double m_xProgress = 0;
 	private double m_exponent = 0.0;
 	private double m_snapTime = 0.0;
 	
 	private int m_cameraAtRestFrameCount = 1;
 	
 	private final smCamera m_camera;
 	private final smGridManager m_gridMngr;
 	
 	private double m_minSnapTime = 0;
 	private double m_snapTimeRange = 0;
 	
 	public smCameraManager(smGridManager gridMngr, smCamera camera, double minSnapTime, double snapTimeRange)
 	{
 		m_gridMngr = gridMngr;
 		m_minSnapTime = minSnapTime;
 		m_snapTimeRange = snapTimeRange;
 		
 		m_camera = camera;
 	}
 	
 	public smCamera getCamera()
 	{
 		return m_camera;
 	}
 	
 	public smPoint getTargetPosition()
 	{
 		return m_targetPosition;
 	}
 	
 	private double calcY(double x)
 	{
 		double y = Math.pow(x*(1/m_snapTime), m_exponent);
 		
 		return y;
 	}
 	
 	public double getSnapProgress()
 	{
 		if( m_snapTime > 0 ) // just being safe
 		{
 			return 1 - (m_xProgress / m_snapTime);
 		}
 		
 		return 0;
 	}
 	
 	public void update(double timeStep)
 	{
 		if ( m_xProgress == 0 )
 		{
 			m_cameraAtRestFrameCount++;
 			m_snapTime = 0;
 			
 			return;
 		}//s_logger.severe(m_camera.getPosition() + "");
 
 		m_xProgress -= timeStep;
 		
 		if ( m_xProgress <= 0 )
 		{
 			this.setCameraPosition(m_targetPosition, true);
 			
 			return;
 		}
 		
 		double progressRatio = calcY(m_xProgress);
 		progressRatio = 1 - progressRatio / m_startY;
 		
 		m_utilVector.copy(m_diffVector);
 		m_utilVector.scaleByNumber(progressRatio);
 		
 		m_camera.getPosition().copy(m_cameraOrigin);
 		m_camera.getPosition().add(m_utilVector);
 		
 		m_camera.update();
 	}
 	
 	private void constrainZ(smPoint point_out)
 	{
 		double maxZ = m_camera.calcMaxZ();
 		
 		//--- DRK < Constrain Z position.
 		if( point_out.getZ() > maxZ )
 		{
 			point_out.setZ(maxZ);
 		}
 		else if( point_out.getZ() < 0 )
 		{
 			point_out.setZ(0);
 		}
 	}
 	
 	public void shiftCamera(double deltaX, double deltaY)
 	{
 		m_camera.getPosition().inc(deltaX, deltaY, 0);
 		m_targetPosition.inc(deltaX, deltaY, 0);
 		m_cameraOrigin.inc(deltaX, deltaY, 0);
 	}
 	
 	public void setTargetPosition(smPoint point, boolean instant)
 	{
 		smA_Grid grid = m_gridMngr.getGrid();
 		
 		smPoint oldTargetPosition = m_utilPoint1;
 		oldTargetPosition.copy(m_targetPosition);
 		
 		//--- DRK > This is here so that given target points can selectively choose which
 		//---		components they want to update. For example a zoom action might set x
 		//---		and y to NaN values because it only wants to update z.
 		for( int i = 0; i < 3; i++ )
 		{
 			double sourceComponent = point.getComponent(i);
 			if( !Double.isNaN(sourceComponent) )
 			{
 				m_targetPosition.setComponent(i, sourceComponent);
 			}
 		}
 		
 		this.constrainZ(m_targetPosition);
 		
 		if( instant )
 		{
			m_cameraAtRestFrameCount = 2;
 			
 			oldTargetPosition.calcDifference(m_targetPosition, m_utilVector);
 			if( m_utilVector.calcLengthSquared() < SNAP_TOLERANCE ) // kinda hacky
 			{
 				this.setCameraPosition(m_targetPosition, false); // just make sure target exactly matches camera position
 				
 				return; // let the camera continue along its trajectory, instead of recalculating it.
 				// NOTE: DRK > Don't know what above comment means now...maybe just a copy/paste from another location?
 			}
 			
 			this.setCameraPosition(m_targetPosition, false);
 			
 			return;
 		}
 		
 		m_cameraOrigin.copy(m_camera.getPosition());
 		m_targetPosition.calcDifference(m_cameraOrigin, m_diffVector);
 		
 		double lengthSquared = m_diffVector.calcLengthSquared();
 		
 		if ( lengthSquared < SNAP_TOLERANCE)
 		{
 			this.setCameraPosition(m_targetPosition, false); // just make sure target exactly matches camera position
 			
 			return;
 		}
 
 		m_lengthToTravel = Math.sqrt(lengthSquared);
 		
 		//TODO: Leaving this constant here cause it's prolly not a final solution anyway.
 		//		Obviously should be moved out someplace that's a little more configurable.
 		double maxDistance = 1024 * Math.max(grid.getCellWidth(), grid.getCellHeight());
 		
 		double distanceRatio = m_lengthToTravel / maxDistance;
 		
 		if( distanceRatio < 1 )
 		{
 			distanceRatio = Math.pow(distanceRatio, 1.0/3.0);
 		}
 		
 		if( !instant )
 		{
 			double timeToTravel = m_minSnapTime + distanceRatio * m_snapTimeRange;
 			
 			m_snapTime = timeToTravel;
 		}
 		else
 		{
 			//m_snapTime = 0.01;
 		}
 		
 		//timeToTravel = smU_Math.clamp(timeToTravel, smS_App.MIN_SNAP_TIME, smS_App.MAX_SNAP_TIME);
 
 		
 		
 		final double MIN_EXPONENT = 3;
 		final double MAX_EXPONENT = 5;
 		final double EXPONENT_RANGE = MAX_EXPONENT - MIN_EXPONENT;
 		
 		m_exponent = MIN_EXPONENT + distanceRatio * EXPONENT_RANGE;
 		//m_exponent = smU_Math.clamp(m_exponent, MIN_EXPONENT, MAX_EXPONENT);
 
 		m_startY = calcY(m_snapTime);
 		
 		m_xProgress = m_snapTime;
 		
 		//s_logger.info(m_exponent + " " + m_snapTime + " " + m_startY);
 		//s_logger.info(m_lengthToTravel + " " + distanceRatio + " distanceRatio");
 		
 		m_cameraAtRestFrameCount = 0;
 	}
 	
 	//--- DRK > NOTE: Must make sure to manually update the cell buffer manager if necessary after this call.
 	public void setCameraPosition(smPoint point, boolean enforceZConstraints)
 	{
 		m_targetPosition.copy(point);
 		
 		if( enforceZConstraints )
 		{
 			constrainZ(m_targetPosition);
 		}
 		
 		m_camera.getPosition().copy(m_targetPosition);
 		
 		m_camera.update();
 		
 		m_cameraOrigin.copy(m_targetPosition);
 		
 		m_xProgress = 0;
 	}
 	
 	public boolean didCameraJustComeToRest()
 	{
 		return m_cameraAtRestFrameCount == 1;
 	}
 	
 	public boolean isCameraAtRest()
 	{
 		if( m_cameraAtRestFrameCount >= 1 )
 		{
 			smU_Debug.ASSERT(m_targetPosition.isEqualTo(m_camera.getPosition(), smTolerance.EXACT), "isCameraAtRest1");
 			
 			return true;
 		}
 
 		return false;
 	}
 }
