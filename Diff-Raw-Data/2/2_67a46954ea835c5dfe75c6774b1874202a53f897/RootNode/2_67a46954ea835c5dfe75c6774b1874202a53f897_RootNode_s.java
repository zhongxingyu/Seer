 package com.mitsugaru.wildguard.config.node;
 
 import com.mitsugaru.wildguard.services.ConfigNode;
 
 public enum RootNode implements ConfigNode {
    /**
     * Protection
     */
    PROTECTION_PLACE_WHITELIST("protection.place.useAsWhitelist", VarType.BOOLEAN, false),
   PROTECTION_BREAK_WHITELIST("protection.bread.useAsWhitelist", VarType.BOOLEAN, false),
    /**
     * Version.
     */
    VERSION("version", VarType.STRING, "0.01");
 
    private String path;
    private VarType type;
    private Object def;
 
    private RootNode(String path, VarType type, Object def) {
       this.path = path;
       this.type = type;
       this.def = def;
    }
 
    @Override
    public String getPath() {
       return path;
    }
 
    @Override
    public VarType getVarType() {
       return type;
    }
 
    @Override
    public Object getDefaultValue() {
       return def;
    }
 
 }
