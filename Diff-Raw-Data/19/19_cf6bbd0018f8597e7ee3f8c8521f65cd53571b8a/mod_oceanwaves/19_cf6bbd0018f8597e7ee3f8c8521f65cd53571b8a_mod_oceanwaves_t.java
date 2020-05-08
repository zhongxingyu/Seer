public class mod_oceanwaves extends BaseMod {
  public String getVersion() { "Minecraft 1.0.0 mod v0"; }
  private float[] heights = {0.2f,0.3f,0.5f,0.8f,1.0f,1.1f,0.9f,0.3f};

  public void load() {
    // this is the BaseMod method to put all your shit in, like how mod_whatever() used to be.
  }

  public float getHeight(ry world, int posx, int posy, int posz, Random rand) {
    return heights[((world.time % 8)+posx) % 8];
  }
 }
