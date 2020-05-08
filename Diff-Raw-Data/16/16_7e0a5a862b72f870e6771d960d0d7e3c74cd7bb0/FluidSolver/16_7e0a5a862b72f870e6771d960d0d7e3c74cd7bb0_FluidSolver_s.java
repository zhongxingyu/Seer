 package cs567.smoke;
 
 import java.util.ArrayList;
 
 import javax.vecmath.*;
 
 
 /**
  * Jos Stam style fluid solver with vorticity confinement
  * and buoyancy force.
  *
  * @author Alexander McKenzie (Mar 12, 2004); Modified by Doug James
  * (Apr 2007)
  * @version 2
  **/
 
 public class FluidSolver
 {
 	/** Grid dimensions (same as in Constants) */
 	int n, size, N;
 
 	/** Timestep size (same as Constants.dt) */
 	float dt;
 
 	/** Temporary swap grid */
 	float[] tmp;
 
 	/** Density grids */
 	float[] d, dOld;
 
 	/** X-velocity grids */
 	float[] u, uOld, fx, uPrev;
 
 	/** Y-velocity grids */
 	float[] v, vOld, fy, vPrev;
 
 	/** Curl grid */
 	float[] curl;
 	
 	/** Number of frames */
 	int n_STEPS_PER_FRAME = 1;
 
 	/** Smoke control force object. */
 	SmokeControlForces control;
 	
 	/** Rigid Body Lists */
 	ArrayList <RigidBody> RB = new ArrayList <RigidBody>();  
 
 	/**
 	 * Set the grid size and timestep, and builds simulation arrays.
 	 **/
 	public FluidSolver() //int n, float dt)
 	{
 		/// USE VALUES FROM Constants --- not good programming
 		/// practice, but it will make your life easier, I hope!
 		/// (Change it if you dislike it.)
 		n    = Constants.n;
 		dt   = Constants.dt;
 		N    = Constants.N;//(n+2)
 		size = Constants.size;// N*N
 
 		reset();
 	}
 
 	/** Specify smoke control object. */
 	public void setSmokeControl(SmokeControlForces control)
 	{
 		this.control = control;
 	}
 
 	/** n-by-n pressure grid. */
 	int n() { return n; }
 
 	/** Size of grid arrays, (n+2)*(n+2). */
 	int size() { return size; }
 
 
 	/**
 	 * Reset the datastructures.
 	 * We use 1d arrays for speed.
 	 **/
 
 	public void reset()
 	{
 		if(d==null)    d    = new float[size];
 		if(dOld==null) dOld = new float[size];
 		if(u==null)    u    = new float[size];
 		if(uOld==null) uOld = new float[size];
 		if(v==null)    v    = new float[size];
 		if(vOld==null) vOld = new float[size];
 		if(curl==null) curl = new float[size];
 		if(fx==null)   fx   = new float[size];
 		if(fy==null)   fy   = new float[size];
 		if(uPrev==null) uPrev = new float[size];
 		if(vPrev==null) vPrev = new float[size];
 
 
 		for (int i = 0; i < size; i++)
 		{
 			u[i] = uOld[i] = fx[i] = v[i] = vOld[i] = fy[i] = uPrev[i] = vPrev[i] = 0.0f;
 			d[i] = dOld[i] = curl[i] = 0.0f;
 		}
 		
 		for (RigidBody rb : RB) rb.reset();
 
 	}
 
 
 	/**
 	 * Calculate the buoyancy force as part of the velocity solver.
 	 * Fbuoy = -a*d*Y + b*(T-Tamb)*Y where Y = (0,1). The constants
 	 * a and b are positive with appropriate (physically meaningful)
 	 * units. T is the temperature at the current cell, Tamb is the
 	 * average temperature of the fluid grid. The density d provides
 	 * a mass that counteracts the buoyancy force.
 	 *
 	 * In this simplified implementation, we say that the tempterature
 	 * is synonymous with density (since smoke is *hot*) and because
 	 * there are no other heat sources we can just use the density
 	 * field instead of a new, seperate temperature field.
 	 *
 	 * @param Fbuoy Array to store buoyancy force for each cell.
 	 * @param alpha Scaling factor (old: alpha=1)
 	 **/
 
 	public void buoyancy(float[] Fbuoy, float alpha)
 	{
 		float Tamb = 0;
 		float a = 0.000625f;
 		float b = 0.025f;
 
 		// sum all temperatures
 		for (int i = 1; i <= n; i++)
 		{
 			for (int j = 1; j <= n; j++)
 			{
 				Tamb += d[I(i, j)];
 			}
 		}
 
 		// get average temperature
 		Tamb /= (n * n);
 
 		// for each cell compute buoyancy force
 		for (int i = 1; i <= n; i++)
 		{
 			for (int j = 1; j <= n; j++)
 			{
 				Fbuoy[I(i, j)] = alpha * (a * d[I(i, j)] + -b * (d[I(i, j)] - Tamb));
 			}
 		}
 	}
 
 
 	/**
 	 * Calculate the curl at position (i, j) in the fluid grid.
 	 * Physically this represents the vortex strength at the
 	 * cell. Computed as follows: w = (del x U) where U is the
 	 * velocity vector at (i, j).
 	 *
 	 * @param i The x index of the cell.
 	 * @param j The y index of the cell.
 	 **/
 
 	public float curl(int i, int j)
 	{
 		float du_dy = (u[I(i, j + 1)] - u[I(i, j - 1)]) * 0.5f;
 		float dv_dx = (v[I(i + 1, j)] - v[I(i - 1, j)]) * 0.5f;
 
 		return du_dy - dv_dx;
 	}
 
 	/** Fragile reference to the density field. */ 
 	float[] getDensity() { return d; }
 
 	/** Density at valid (i,j) location. */
 	public float getDensity(int i, int j) { 
 		return d[I(i,j)];
 	}
 
 
 	/**
 	 * Calculate the vorticity confinement force for each cell
 	 * in the fluid grid. At a point (i,j), Fvc = N x w where
 	 * w is the curl at (i,j) and N = del |w| / |del |w||.
 	 * N is the vector pointing to the vortex center, hence we
 	 * add force perpendicular to N.
 	 *
 	 * @param Fvc_x The array to store the x component of the
 	 *        vorticity confinement force for each cell.
 	 * @param Fvc_y The array to store the y component of the
 	 *        vorticity confinement force for each cell.
 	 **/
 
 	public void vorticityConfinement(float[] Fvc_x, float[] Fvc_y)
 	{
 		float dw_dx, dw_dy;
 		float length;
 		float v;
 
 		// Calculate magnitude of curl(u,v) for each cell. (|w|)
 		for (int i = 1; i <= n; i++)
 		{
 			for (int j = 1; j <= n; j++)
 			{
 				curl[I(i, j)] = Math.abs(curl(i, j));
 			}
 		}
 
 		for (int i = 2; i < n; i++)
 		{
 			for (int j = 2; j < n; j++)
 			{
 
 				// Find derivative of the magnitude (n = del |w|)
 				dw_dx = (curl[I(i + 1, j)] - curl[I(i - 1, j)]) * 0.5f;
 				dw_dy = (curl[I(i, j + 1)] - curl[I(i, j - 1)]) * 0.5f;
 
 				// Calculate vector length. (|n|)
 				// Add small factor to prevent divide by zeros.
 				length = (float) Math.sqrt(dw_dx * dw_dx + dw_dy * dw_dy)
 						+ 0.000001f;
 
 				// N = ( n/|n| )
 				dw_dx /= length;
 				dw_dy /= length;
 
 				v = curl(i, j);
 
 				// N x w
 				Fvc_x[I(i, j)] = dw_dy * -v;
 				Fvc_y[I(i, j)] = dw_dx *  v;
 			}
 		}
 	}
 
 
 	/**
 	 * The basic velocity solving routine as described by Stam, with
 	 * some hooks for target-driven smoke control.
 	 **/
 
 	public void velocitySolver()
 	{
 		for(int i=0; i<size; i++)  fx[i] = fy[i] = 0; 		
 		for(int ij=0; ij<size; ij++){ uPrev[ij]=u[ij]; vPrev[ij]=v[ij]; }
 
 		
 		// add velocity that was input by mouse
 		addSource(u, uOld);
 		addSource(v, vOld);
 		add(fx,uOld);
 		add(fy,vOld);
 
 
 		if(control != null)  {/// CONTROL:
 			// ADD IN DRIVING FORCE:
 			control.getDrivingForce(uOld, vOld, d); 
 
 			addSource(u, uOld);
 			addSource(v, vOld);
 			add(fx,uOld);
 			add(fy,vOld);
 			
 
 		}
 		
 
 		
 		// DAMP MOMENTUM:
 		for(int ij=0; ij<size; ij++) {
 			u[ij] -= Constants.V_d * dt * u[ij];
 			v[ij] -= Constants.V_d * dt * v[ij] ;
 			fx[ij] -= Constants.V_d * u[ij];
 			fy[ij] -= Constants.V_d * v[ij]  ;
 		}
 
 		// add in vorticity confinement force
 		vorticityConfinement(uOld, vOld);
 
 		addSource(u, uOld);
 		addSource(v, vOld);
 		add(fx,uOld);
 		add(fy,vOld);
 
 		// add in buoyancy force
 		//buoyancy(vOld, Constants.BUOYANCY);
 		//addSource(v, vOld);
 		//add(fy,vOld);
 		
 		// swapping arrays for economical mem use
 		// and calculating diffusion in velocity.
 		if(Constants.VISCOSITY > 0) {
 			swapU();
 			diffuse(0, u, uOld, Constants.VISCOSITY);
 
 			swapV();
 			diffuse(0, v, vOld, Constants.VISCOSITY);
 		}
 
 		
 		// we create an incompressible field
 		// for more effective advection.
 		project(u, v, uOld, vOld);
 		swapU(); swapV();
 		
 
 		// self-advect velocities (SLOTH: treated individually)
 		advect(1, u, uOld, uOld, vOld);
 		advect(2, v, vOld, uOld, vOld);
 
 		// make an incompressible field
 		project(u, v, uOld, vOld);
 		project(fx,fy,uOld, vOld);
 		
 		
 		// MAKE UPDATE FOR THE RIGID BODIES CELLS
 		// APPLY RIGID BODY FORCES
 		for(RigidBody rb : RB) rb.applyWrenchW(new Vector2d(0,-10*rb.mass), 0);  //Add gravity force to each rigid body
 		
 		// ADD CONSTRAINT FORCES
 		for(RigidBody rb : RB) rb.applyConstraintForces();  //Add gravity force to each rigid body
 
 		rigidSolver(u, v, uPrev, vPrev);
 
 		// clear all input velocities for next frame
 		for(int i=0; i<size; i++)  {
 			uOld[i] = vOld[i] = 0; 
 		}
 
 
 	}
 
 	/** Update the velocities for the rigid bodies' cells.  See Carleson et al, Eqs 17 and 18
 	 * 
 	 * @param u
 	 * @param v
 	 */
 	public void rigidSolver(float[] u, float[] v, float[] uPrev, float[] vPrev){
 
 		
 		//Compute Advection term 
 		float[] advectionTermU = new float[size]; 
 		float[] advectionTermV = new float[size]; 
 		uDotDel(u,u,v,advectionTermU);
 		uDotDel(v,u,v,advectionTermV);
 
 
 		for (RigidBody rb: RB){
 						
 			//Accumulate velocities in rb.v.  These will be scaled appropriately before being use
 			//to perform the final position update.
 			rb.v.set(0.0,0.0);
 			rb.omega = 0;
 			
 			//float mass =0 ;
 			//float momentOfInertia = 0;
 			for (int i = 1; i <= n; i++)
 			{
 				for (int j = 1; j <= n; j++)
 				{
 					double w = rb.wRatio(i, j);
 					if (w > 0){
 						//FINDING S USING EQUATION (17) FROM THE CARLSON PAPER
 						
 						//seperation between cell and x_cm
 						Vector2d r = new Vector2d(i+0.5 - rb.x.x, j+0.5- rb.x.y);  
 
 						// collision portion
 						Vector2d rbAcceleration = rb.getAcceleration(r); 
 
 						// COMPUTE CORRECTION FOR DENSE FLUID
 						// density term -(rho_r-rho_f)
 						double relDensity = (rb.density - Constants.FLUID_DENSITY);
 
 						// velocity term (u dot DEL)u
						double uS =  -relDensity * ((u[i]-uPrev[i])/Constants.dt  + advectionTermU[I(i,j)] - fx[I(i,j)] );
						double vS =  -relDensity * ((v[i]-vPrev[i])/Constants.dt  + advectionTermV[I(i,j)] - fy[I(i,j)] );
 
 						//update u and v using S
						u[I(i,j)] += (float) (w * Constants.dt/rb.density * (uS + rb.density*rbAcceleration.x ));
						v[I(i,j)] += (float) (w * Constants.dt/rb.density * (vS + rb.density*rbAcceleration.y ));
 
 						
 						// CALCULATING u_R USING EQUATION (23) FROM THE CARLSON PAPER
 						// updating v
 						rb.v.x += rb.density * u[I(i,j)] * w; 
 						rb.v.y += rb.density * v[I(i,j)] * w;
 						//mass += rb.density * w ;
 
 						// updating w
 						rb.omega += rb.density * (r.x*v[I(i,j)] - r.y*u[I(i,j)] ) * w  ;
 						//momentOfInertia += rb.density * r.lengthSquared() * w;
 
 					}
 				}
 			}
 
 			//divide out mass and moment of inertia
 			rb.v.scale(rb.getInverseMass());
 			rb.omega *= rb.getInverseMomentOfInertia();
 			
 			for (int i = 1; i <= n; i++)
 			{
 				for (int j = 1; j <= n; j++)
 				{
 					double w = rb.wRatio(i, j);
 					if (w > 0){
 						Vector2d r = new Vector2d(i+0.5 - rb.x.x, j+0.5- rb.x.y); //seperation vector
 						double u_UR = rb.v.x - rb.omega*r.y;
 						double v_UR = rb.v.y + rb.omega*r.x;
 						
 						//UPDATING u AND v USING EQUATION (26) FROM THE CARLSON PAPER
 						u[I(i,j)] = (float) ((1 - w)*u[I(i,j)] + w*(u_UR)) ;
 						v[I(i,j)] = (float) ((1 - w)*v[I(i,j)] + w*(v_UR));
 					}
 				}
 			}
 
 			rb.updatePosition(dt * n);
 		}
 	}	
 	
 	/**
 	 * Compute (u dot Del) f at each grid point
 	 * @param result Array to store the result 
 	 * @param f a scalar field to which we apply u dot del 
 	 * @param u x-component of vector field u
 	 * @param v y-comonent of vector field u
 	 */
 	public void uDotDel( float[] f , float[] u, float[] v, float[] result ){
 
 		for (int i = 1; i <= n; i++)
 		{
 			for (int j = 1; j <= n; j++)
 			{
 		
 				// Using MAC grid: field on the edge;
 				double f_right = (f[I(i,j)] + f[I(i+1, j)])/2.0;
 				double f_left = (f[I(i,j)] + f[I(i-1, j)])/2.0;
 				double f_down = (f[I(i,j)] + f[I(i, j-1)])/2.0;
 				double f_up = (f[I(i,j)] + f[I(i, j+1)])/2.0;
 				
 				result[I(i,j)] = (float) (u[I(i,j)]*(f_right-f_left) + v[I(i,j)]*(f_up-f_down));
 			}
 		}
 	}
 	
 	/**
 	 * The basic density solving routine.
 	 **/
 
 	public void densitySolver()
 	{
 		// add density inputted by mouse
 		addSource(d, dOld);
 		swapD();
 
 		if(Constants.SMOKE_DIFFUSION > 0) {
 			diffuse(0, d, dOld, Constants.SMOKE_DIFFUSION);
 			swapD();
 		}
 
 		advect(0, d, dOld, u, v);
 
 		/// GATHER SMOKE, dOld = v_g*G:
 		if(control != null) {
 			float[] rate = dOld;
 			control.getGatheringRate(rate, d);
 			for(int i=0; i<size; i++) d[i] += dt * rate[i];
 
 			control.normalizeDensity(d);
 		}
 
 		// clear input density array for next frame
 		for(int i=0; i<size; i++)  dOld[i] = 0;
 	}
 
 
 
 	/** x += dt * x0 */
 	private void addSource(float[] x, float[] x0)
 	{
 		for (int i=0; i<size; i++)
 			x[i] += dt * x0[i];
 	}
 
 	/** x +=  x0 */
 	private void add(float[] x, float[] x0)
 	{
 		for (int i=0; i<size; i++)
 			x[i] += x0[i];
 	}
 
 
 	/**
 	 * Calculate the input array after advection. We start with an
 	 * input array from the previous timestep and an and output array.
 	 * For all grid cells we need to calculate for the next timestep,
 	 * we trace the cell's center position backwards through the
 	 * velocity field. Then we interpolate from the grid of the previous
 	 * timestep and assign this value to the current grid cell.
 	 *
 	 * @param b Flag specifying how to handle boundries.
 	 * @param d Array to store the advected field.
 	 * @param d0 The array to advect.
 	 * @param du The x component of the velocity field.
 	 * @param dv The y component of the velocity field.
 	 **/
 
 	private void advect(int b, float[] d, float[] d0, float[] du, float[] dv)
 	{
 		int i0, j0, i1, j1;
 		float x, y, s0, t0, s1, t1, dt0;
 
 		dt0 = dt * n;
 
 		for (int i = 1; i <= n; i++)
 		{
 			for (int j = 1; j <= n; j++)
 			{
 				// go backwards through velocity field
 				x = i - dt0 * du[I(i, j)];
 				y = j - dt0 * dv[I(i, j)];
 
 				// interpolate results
 				if (x > n + 0.5) x = n + 0.5f;
 				if (x < 0.5)     x = 0.5f;
 
 				i0 = (int) x;
 				i1 = i0 + 1;
 
 				if (y > n + 0.5) y = n + 0.5f;
 				if (y < 0.5)     y = 0.5f;
 
 				j0 = (int) y;
 				j1 = j0 + 1;
 
 				s1 = x - i0;
 				s0 = 1 - s1;
 				t1 = y - j0;
 				t0 = 1 - t1;
 
 				d[I(i, j)] = s0 * (t0 * d0[I(i0, j0)] + t1 * d0[I(i0, j1)])
 						+ s1 * (t0 * d0[I(i1, j0)] + t1 * d0[I(i1, j1)]);
 
 			}
 		}
 		setBoundary(b, d);
 	}
 
 
 
 	/**
 	 * Recalculate the input array with diffusion effects.
 	 * Here we consider a stable method of diffusion by
 	 * finding the densities, which when diffused backward
 	 * in time yield the same densities we started with.
 	 * This is achieved through use of a linear solver to
 	 * solve the sparse matrix built from this linear system.
 	 *
 	 * @param b Flag to specify how boundries should be handled.
 	 * @param c The array to store the results of the diffusion
 	 * computation.
 	 * @param c0 The input array on which we should compute
 	 * diffusion.
 	 * @param diff The factor of diffusion.
 	 **/
 	private void diffuse(int b, float[] c, float[] c0, float diff)
 	{
 		float a = dt * diff * n * n;
 		linearSolver(b, c, c0, a, 1 + 4 * a);
 	}
 
 
 	/**
 	 * Use project() to make the velocity a mass conserving,
 	 * incompressible field. Achieved through a Hodge
 	 * decomposition. First we calculate the divergence field
 	 * of our velocity using the mean finite differnce approach,
 	 * and apply the linear solver to compute the Poisson
 	 * equation and obtain a "height" field. Now we subtract
 	 * the gradient of this field to obtain our mass conserving
 	 * velocity field.
 	 *
 	 * @param x The array in which the x component of our final
 	 * velocity field is stored.
 	 * @param y The array in which the y component of our final
 	 * velocity field is stored.
 	 * @param p A temporary array we can use in the computation.
 	 * @param div Another temporary array we use to hold the
 	 * velocity divergence field.
 	 *
 	 **/
 	void project(float[] x, float[] y, float[] p, float[] div)
 	{
 		for (int i = 1; i <= n; i++)
 		{
 			for (int j = 1; j <= n; j++)
 			{
 				div[I(i, j)] = (x[I(i+1, j)] - x[I(i-1, j)]
 						+ y[I(i, j+1)] - y[I(i, j-1)])
 						* - 0.5f / n;
 				p[I(i, j)] = 0;
 			}
 		}
 
 		setBoundary(0, div);
 		setBoundary(0, p);
 
 		linearSolver(0, p, div, 1, 4);
 
 		for (int i = 1; i <= n; i++)
 		{
 			for (int j = 1; j <= n; j++)
 			{
 				x[I(i, j)] -= 0.5f * n * (p[I(i+1, j)] - p[I(i-1, j)]);
 				y[I(i, j)] -= 0.5f * n * (p[I(i, j+1)] - p[I(i, j-1)]);
 			}
 		}
 
 		setBoundary(1, x);
 		setBoundary(2, y);
 	}
 
 	/**
 	 * Iterative linear system solver using the Gauss-Seidel
 	 * relaxation technique. 
 	 * 
 	 * TODO: REPLACE THIS METHOD WITH BETTER PCG SOLVER!
 	 **/
 	void linearSolver(int b, float[] x, float[] x0, float a, float c)
 	{
 		int nIterations = Constants.N_GAUSS_SEIDEL_ITERATIONS;
 
 		float h        = 1.f/(float)n;
 		float omegaSOR = 2f - 0.2f * 100f * h;
 		omegaSOR = 1f;
 		float oneMinus_omegaSOR = 1.f - omegaSOR;
 
 		for (int k = 0; k < nIterations; k++)
 		{
 			for (int i = 1; i <= n; i++)
 			{
 				for (int j = 1; j <= n; j++)
 				{
 					float est  = (a * ( x[I(i-1, j)] + x[I(i+1, j)]
 							+   x[I(i, j-1)] + x[I(i, j+1)])
 							+  x0[I(i, j)]) / c;
 					float old  = x[I(i,j)];
 					x[I(i,j)]  = omegaSOR*est + oneMinus_omegaSOR*old;
 				}
 			}
 			setBoundary(b, x);
 		}
 	}
 
 
 	/** Specifies simple boundary conditions. */
 	private void setBoundary(int b, float[] x)
 	{
 		for (int i = 1; i <= n; i++)
 		{
 			x[I(  0, i  )] = b == 1 ? -x[I(1, i)] : x[I(1, i)];
 			x[I(n+1, i  )] = b == 1 ? -x[I(n, i)] : x[I(n, i)];
 			x[I(  i, 0  )] = b == 2 ? -x[I(i, 1)] : x[I(i, 1)];
 			x[I(  i, n+1)] = b == 2 ? -x[I(i, n)] : x[I(i, n)];
 		}
 
 		x[I(  0,   0)] = 0.5f * (x[I(1, 0  )] + x[I(  0, 1)]);
 		x[I(  0, n+1)] = 0.5f * (x[I(1, n+1)] + x[I(  0, n)]);
 		x[I(n+1,   0)] = 0.5f * (x[I(n, 0  )] + x[I(n+1, 1)]);
 		x[I(n+1, n+1)] = 0.5f * (x[I(n, n+1)] + x[I(n+1, n)]);
 
 	}
 
 	/** util array swapping method */
 	public void swapU(){ tmp = u; u = uOld; uOld = tmp; }
 	/** util array swapping method */
 	public void swapV(){ tmp = v; v = vOld; vOld = tmp; }
 	/** util array swapping method */
 	public void swapD(){ tmp = d; d = dOld; dOld = tmp; }
 
 	/**  UTIL method for indexing 1d arrays */
 	public final int I(int i, int j){ return i + N*j; }
 
 	public void addRigidBodies(ArrayList<RigidBody> rbs) {
 		RB = rbs;
 	}
 
 	public void setNumerofFrame(int n_STEPS_PER_FRAME) {
 		// TODO Auto-generated method stub
 		this.n_STEPS_PER_FRAME = n_STEPS_PER_FRAME;
 	}
 }
