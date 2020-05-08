 package no.ebakke.studycaster.screencasting;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 import no.ebakke.studycaster.api.StudyCaster;
 
 public final class Quilt {
   // Non-overlapping list of rectangles, each either positive or negative.
   private List<Patch> patches = new ArrayList<Patch>();
   /* TODO: Make it actually go to the edges, but make really sure there is no
   problems with overflow first. */
  private static final int MINVAL = Integer.MIN_VALUE / 256;
   private static final int MAXVAL = Integer.MAX_VALUE / 256;
   private static final Rectangle PLANE =
       new Rectangle(MINVAL, MINVAL, MAXVAL, MAXVAL);
 
   public Quilt() {
     addPatch(PLANE, false);
   }
 
   public Quilt(Rectangle rect) {
     this();
     addPatch(rect, true);
   }
 
   private static void addIntersection(List<Patch> result, Patch original,
       int x1, int y1, int x2, int y2)
   {
     // TODO: Handle overflow case here.
     Rectangle rect =
         new Rectangle(x1, y1, x2-x1, y2-y1).intersection(original.rect);
     if (!rect.isEmpty())
       result.add(new Patch(rect, original.positive));
   }
 
   private static void subtractPatch(
       Patch original, Patch subtractMe, List<Patch> res)
   {
    // TODO: Get rid of these residual constants.
    final Rectangle a = original.rect;
    final Rectangle b = subtractMe.rect;
    final int Ax1 = a.x     , Bx1 = b.x;
    final int Ay1 = a.y     , By1 = b.y;
    final int Aw  = a.width , Bw  = b.width;
    final int Ah  = a.height, Bh  = b.height;
    final int Ax2 = Ax1 + Aw, Bx2 = Bx1 + Bw;
    final int Ay2 = Ay1 + Ah, By2 = By1 + Bh;
    addIntersection(res, original, MINVAL, MINVAL, MAXVAL,    By1);
    addIntersection(res, original, MINVAL,    By2, MAXVAL, MAXVAL);
    addIntersection(res, original, MINVAL,    By1,    Bx1,    By2);
    addIntersection(res, original,    Bx2,    By1, MAXVAL,    By2);
   }
 
   public void addPatch(Rectangle rect, boolean positive) {
     Patch newPatch = new Patch(rect, positive);
     List<Patch> oldPatches = patches;
     patches = new ArrayList<Patch>(oldPatches.size() + 5);
     for (Patch oldPatch : oldPatches)
       subtractPatch(oldPatch, newPatch, patches);
     patches.add(newPatch);
   }
 
   public boolean contains(int x, int y) {
     for (Patch patch : patches) {
       if (patch.rect.contains(x, y))
         return patch.positive;
     }
     return false;
   }
 
   /* Returns positive or negative int, at least |ret|>0. */
   public int getHorizontalRunLength(int x, int y) {
     for (Patch patch : patches) {
       if (patch.rect.contains(x, y)) {
         return (patch.rect.x + patch.rect.width - x) *
             (patch.positive ? 1 : -1);
       }
     }
     // TODO: This is actually an unanticipated error case, but don't make it a
     //       showstopper for now.
     StudyCaster.log.warning("Quilt assertion fail");
     return -100;
   }
 
   private static final class Patch {
     Rectangle rect;
     boolean   positive;
 
     public Patch(Rectangle rect, boolean positive) {
       this.rect     = rect;
       this.positive = positive;
     }
   }
 }
