 package rgbdslam;
 
 import java.util.*;
 
 import april.jmat.*;
 
 import kinect.*;
 
 /** A structure to store colored point cloud data
  *  in voxel space of arbitrary resolution.
  */
 public class VoxelArray
 {
     HashMap<VoxelKey, Voxel> voxels = new HashMap<VoxelKey, Voxel>();
     double resolution;
 
     public VoxelArray(double resolution_)
     {
         resolution = resolution_;
     }
 
     public void voxelizePointCloud(ColorPointCloud cpc, Matrix rbt)
     {
         for (int i = 0; i < cpc.numPoints(); i++) {
             double[] xyz = LinAlg.transform(rbt, cpc.points.get(i));
             VoxelKey vk = new VoxelKey(xyz);
             if (!voxels.containsKey(vk))
                 voxels.put(vk, new Voxel(cpc.colors.get(i)));
             else
                 voxels.get(vk).addSample(cpc.colors.get(i));
         }
     }
 
     // XXX Needs verification. Unit tests...go!
     /** Given another voxel array and a rigid body
      *  transform between our local coordinates and
      *  the other array's coordinates (rbt maps a
      *  coodinate in va-local to our local), make
      *  a merged voxel array
      **/
     public VoxelArray merge(VoxelArray va, Matrix rbt)
     {
         assert (va.resolution == resolution);
 
         // Transform the other voxels to our space
         for (VoxelKey vk: va.voxels.keySet()) {
             VoxelKey vk_xform = vk.transform(rbt);
             if (!voxels.containsKey(vk_xform)) {
                 voxels.put(vk_xform, va.voxels.get(vk).copy());
             } else {
                 voxels.get(vk_xform).merge(va.voxels.get(vk).copy());
             }
         }
 
         return this;
     }
 
     public VoxelArray copy()
     {
         VoxelArray va = new VoxelArray(resolution);
 
         for (VoxelKey vk: voxels.keySet()) {
             va.voxels.put(vk, voxels.get(vk).copy());
         }
 
         return va;
     }
 
     class VoxelKey
     {
         int[] xyz;
 
         public VoxelKey(double[] xyz_)
         {
             xyz = new int[3];
             xyz[0] = (int)(xyz_[0]/resolution);
             xyz[1] = (int)(xyz_[1]/resolution);
             xyz[2] = (int)(xyz_[2]/resolution);
         }
 
         public VoxelKey transform(Matrix rbt)
         {
             double[] xyz_xform = new double[] {xyz[0]+resolution/2,
                                                xyz[1]+resolution/2,
                                                xyz[2]+resolution/2};
             xyz_xform = LinAlg.transform(rbt, xyz_xform);
 
             return new VoxelKey(xyz_xform);
         }
 
         public int hashCode()
         {
            return Arrays.hashCode(xyz);
         }
 
         public boolean equals(Object o)
         {
             if (o == null)
                 return false;
             if (!(o instanceof VoxelKey))
                 return false;
             VoxelKey vk = (VoxelKey)o;
             return Arrays.equals(vk.xyz, xyz);
         }
     }
 }
