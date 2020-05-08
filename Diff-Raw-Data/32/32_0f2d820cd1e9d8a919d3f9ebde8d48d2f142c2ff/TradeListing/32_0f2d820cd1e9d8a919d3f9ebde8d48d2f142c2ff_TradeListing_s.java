 package com.dustyneuron.txmarket.schema;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 public class TradeListing {
     TransactionSchemaProtos.TradeListing data;
     byte[] peerId;
     
     private TradeListing(TransactionSchemaProtos.TradeListing data, byte[] peerId) {
         this.data = data;
         this.peerId = peerId;
     }
 
     public TradeListing(List<SinglePartyData> newPartyData, TradeListing parent) {
         this(
             TransactionSchemaProtos.TradeListing.newBuilder(parent.getData())
                 .addAllAllPartiesData(newPartyData).build(),
                 parent.peerId);
     }
     
     public TradeListing(List<SinglePartyData> newPartyData, byte[] peerId) {
         this(
             TransactionSchemaProtos.TradeListing.newBuilder()
                 .addAllAllPartiesData(newPartyData)
                 .setPeerId(ByteString.copyFrom(peerId))
                 .build(),
             peerId);
     }
         
     public static TradeListing parseFrom(byte[] data, byte[] peerId) throws InvalidProtocolBufferException {
         return new TradeListing(TransactionSchemaProtos.TradeListing.parseFrom(data), peerId);
     }
     
     public byte[] getPeerId() {
         return peerId;
     }
     
     public TransactionSchemaProtos.TradeListing getData() {
         return data;
     }
     
     public List<DataItem> getDataForBlinding() {
         List<DataItem> results = new ArrayList<DataItem>();
         for (SinglePartyData p : data.getAllPartiesDataList()) {
             for (DataItem d : p.getDataList()) {
                 if (SchemaUtils.usingBlinding(d)) {
                     results.add(d);
                 }
             }
         }
         return results;
     }
 
     public boolean containsPrivateData() {
         for (SinglePartyData p : data.getAllPartiesDataList()) {
             for (DataItem d : p.getDataList()) {
                 if (SchemaUtils.usingBlinding(d) && d.hasContent()) {
                     return true;
                 }
             }
         }
         return false;
     }
     
     public boolean usingBlinding() {
         for (SinglePartyData p : data.getAllPartiesDataList()) {
             for (DataItem d : p.getDataList()) {
                 if (SchemaUtils.usingBlinding(d)) {
                     return true;
                 }
             }
         }
         return false;
     }
     
     public TradeListing createBlindListing() {
         List<SinglePartyData> newPartyData = new ArrayList<SinglePartyData>();
 
         for (SinglePartyData p : data.getAllPartiesDataList()) {
             SinglePartyData.Builder newP = SinglePartyData.newBuilder(p);
             newP.clearData();
             newP.addAllData(SchemaUtils.createBlindData(p.getDataList()));
 
             newPartyData.add(newP.build());
         }
 
         return new TradeListing(newPartyData, this);
     }
     
     @Override
     public String toString() {
         String s = "";
         for (SinglePartyData d : data.getAllPartiesDataList()) {
             s += "party #" + data.getAllPartiesDataList().indexOf(d) + "\n";
             s += d.toString() + "\n";
         }
         return s;
     }
 }
