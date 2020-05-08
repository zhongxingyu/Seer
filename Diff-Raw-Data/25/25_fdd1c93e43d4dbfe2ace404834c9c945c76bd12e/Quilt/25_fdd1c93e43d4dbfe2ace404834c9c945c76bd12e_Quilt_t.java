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
   private static final int MAXVAL = Integer.MAX_VALUE / 256;
  private static final int MINVAL = -MAXVAL / 2;
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
    final Rectangle sM = subtractMe.rect;
    // original - subtractMe = original * (1 - subtractMe)
    addIntersection(res, original,          MINVAL,           MINVAL, MAXVAL,                sM.y);
    addIntersection(res, original,          MINVAL, sM.y + sM.height, MAXVAL,              MAXVAL);
    addIntersection(res, original,          MINVAL,             sM.y,   sM.x,    sM.y + sM.height);
    addIntersection(res, original, sM.x + sM.width,             sM.y, MAXVAL,    sM.y + sM.height);
   }
 
   public void addPatch(Rectangle rect, boolean positive) {
     Patch newPatch = new Patch(rect, positive);
     List<Patch> oldPatches = patches;
     patches = new ArrayList<Patch>(oldPatches.size() + 5);
    // Subtract from all existing patches first to avoid overlapping.
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
