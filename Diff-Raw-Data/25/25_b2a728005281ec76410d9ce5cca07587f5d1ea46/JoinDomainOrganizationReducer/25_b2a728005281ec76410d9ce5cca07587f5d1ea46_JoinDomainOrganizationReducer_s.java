 package org.shirdrn.kodz.inaction.hadoop.join.reduceside;
 
 import java.io.IOException;
 import java.util.Iterator;
 
import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.shirdrn.kodz.inaction.hadoop.join.DomainDetail;
 
 public class JoinDomainOrganizationReducer extends
 		Reducer<OrganizationIdCompositeKey, Text, OrganizationIdCompositeKey, DomainDetail> {
 
 	@Override
 	protected void reduce(OrganizationIdCompositeKey key, Iterable<Text> values, Context context)
 			throws IOException, InterruptedException {
 		Iterator<Text> iter = values.iterator();
		Text organization = iter.next();
 		while(iter.hasNext()) {
 			Text value = iter.next();
 			String[] fields = value.toString().split("\t");
 			if(fields.length == 2) {
 				DomainDetail detail = new DomainDetail();
 				String domain = fields[0];
 				String ip = fields[1];
 				detail.setDomain(new Text(domain));
 				detail.setIpAddress(new Text(ip));
 				detail.setOrganization(organization);
//				IntWritable orgId = new IntWritable(key.getOrganizationId().get());
 				context.write(key, detail);
 			}
 		}
 	}
 
 }
