 package org.araqne.linux.api;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 public class DiskPartitionTest {
 
 	@Test
 	public void getDiskPartition1() {
 		List<String> lines = new ArrayList<String>();
 		lines.add("/dev/sda1        9156984 5075680   3609496  59% /");
 
 		List<DiskPartition> partitions = DiskPartition.getDiskPartitions(lines);
 		assertEquals(1, partitions.size());
 
 		DiskPartition partition = partitions.get(0);
 		assertEquals("/dev/sda1", partition.getName());
		assertEquals(3609496l * 1024, partition.getAvailable());
 		assertEquals("/", partition.getPath());
		assertEquals(8685176l * 1024, partition.getTotal());
		assertEquals(5075680l * 1024, partition.getUsed());
 	}
 
 	@Test
 	public void getDiskPartition2() {
 		List<String> lines = new ArrayList<String>();
 		lines.add("/dev/mapper/vg_linux-lv_root");
 		lines.add("51609340  45357008");
 		lines.add("5728100  89% /");
 
 		List<DiskPartition> partitions = DiskPartition.getDiskPartitions(lines);
 		assertEquals(1, partitions.size());
 
 		DiskPartition partition = partitions.get(0);
 		assertEquals("/dev/mapper/vg_linux-lv_root", partition.getName());
		assertEquals(5728100l * 1024, partition.getAvailable());
 		assertEquals("/", partition.getPath());
		assertEquals(51085108l * 1024, partition.getTotal());
		assertEquals(45357008l * 1024, partition.getUsed());
 	}
 }
