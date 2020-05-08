 	public static byte[] serializeFingerprint(Fingerprint obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
     }
 
     public static Fingerprint deserializeFingerprint(byte[] bytes) throws IOException, ClassNotFoundException {
         ByteArrayInputStream b = new ByteArrayInputStream(bytes);
         ObjectInputStream o = new ObjectInputStream(b);
         return (Fingerprint) o.readObject();
    }
