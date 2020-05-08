 package cs567.smoke;
 
 /**
  * Compile-time constants/parameters affecting the
  * simulation. Although not great software design, I've collected them
  * here to avoid students/you scanning the codebase looking for all
  * the hidden tweakable parameters.
  *
  * @author Doug James, April 2007.
  */
 public class Constants
 {
     /** Time-step size. Note that explicitly integrated smoke-control
      * forcing/density terms can impose step-size restrictions. (default: 0.005)*/
    public static final float dt = 0.01f;/// <-- MAY NEED TO REDUCE
 
     /** Resolution of N-by-N computational grid. */
    public static final int N = 120;
 
     /** Resolution of (n+2)-by-(n+2) computational grid. */
     public static final int n = N-2;/// <-- DO NOT CHANGE
 
     /** Number of cells in computational domain, and size of
      * grid-based float[] arrays indexed by I(i,j). */
     public static final int size = N*N;/// <-- DO NOT CHANGE
 
     /** Array index function. */
     public static final int I(int i, int j) { return i + N*j; }
 
     
     /** Iterations used in FluidSolver.linearSolver(). */
     public static final int N_GAUSS_SEIDEL_ITERATIONS = N/3;
 
 
     /** Driving force amplitude, V_f (default: 2)  */
     public static final float V_f = 2f;
 
     /** Gathering rate amplitude, V_g (default: 0.0005)  */
     public static final float V_g = 0.2f;
 
     /** Drag force amplitude, V_d (default: 0.5)  */
     public static final float V_d = 0.5f;
 
     /** Smoke diffusion coefficient (default: 0.0001). Nonzero values are needed (I
      * believe) to avoid instabilities during smoke control. */
     public static final float SMOKE_DIFFUSION = 0.00001f;
 
     /** Viscosity (velocity diffusion coefficient). Default is zero
      * since we try hard to get rid of numerical diffusion (e.g.,
      * using vorticity confinement) without adding more, and incurring
      * additional linear system solves. */
     public static final float VISCOSITY = 0f;// ZERO IS OK
 
     /** Smoke buoyancy (dimensionless) (default: 1) */
     public static final float BUOYANCY = 1.f;
     
     /** Fluid density */
     public static final float FLUID_DENSITY = 1F;
 }
