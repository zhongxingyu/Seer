 package fr.kissy.hellion.server.actor.stage;
 
 import akka.io.AbstractSymmetricPipePair;
 import akka.io.PipePairFactory;
 import akka.io.PipelineContext;
 import akka.io.SymmetricPipePair;
 import akka.io.SymmetricPipelineStage;
 import akka.util.ByteIterator;
 import akka.util.ByteString;
 import com.google.common.collect.Lists;
 import com.google.protobuf.CodedInputStream;
 import com.google.protobuf.CodedOutputStream;
 import scala.util.Either;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class Varint32Stage extends SymmetricPipelineStage<PipelineContext, ByteString, ByteString> {
     /**
      * @inheritDoc
      */
     @Override
     public SymmetricPipePair<ByteString, ByteString> apply(final PipelineContext ctx) {
         return PipePairFactory.create(ctx, new AbstractSymmetricPipePair<ByteString, ByteString>() {
             private ByteString buffer = null;
 
             /**
              * @inheritDoc
              */
             @Override
             public Iterable<Either<ByteString, ByteString>> onCommand(ByteString cmd) {
                 final ArrayList<Either<ByteString, ByteString>> res = Lists.newArrayList();
 
                 int length = cmd.length();
                 int varintLength = CodedOutputStream.computeRawVarint32Size(length);
                 byte[] buffer = new byte[varintLength + length];
                 try {
                     CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(buffer);
                     codedOutputStream.writeRawVarint32(length);
                     codedOutputStream.writeRawBytes(cmd.toArray());
                     codedOutputStream.flush();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 
                 res.add(makeCommand(ByteString.fromArray(buffer)));
                 return res;
             }
 
             /**
              * @inheritDoc
              */
             @Override
             public Iterable<Either<ByteString, ByteString>> onEvent(ByteString event) {
                 final ArrayList<Either<ByteString, ByteString>> res = Lists.newArrayList();
                 ByteString current = buffer == null ? event : buffer.concat(event);
 
                 try {
                     final byte[] buf = new byte[5];
                     for (int i = 0; i < buf.length; i ++) {
                         if (current.length() == 0) {
                             buffer = null;
                             return res;
                         }
 
                         buf[i] = current.apply(i);
                         if (buf[i] >= 0) {
                             int prefixLength = i + 1;
                             int length = CodedInputStream.newInstance(buf, 0, prefixLength).readRawVarint32();
                             if (length < 0) {
                                 buffer = null;
                                 return res;
                             }
 
                            if (current.length() - prefixLength < length) {
                                 buffer = current;
                                return res;
                             } else {
                                res.add(makeEvent(current.slice(prefixLength, length + prefixLength)));
                                current = current.drop(prefixLength + length);
                             }
                         }
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 
                 return res;
             }
         });
     }
 }
